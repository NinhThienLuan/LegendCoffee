(function () {
  var sourceEl = document.getElementById("jsonSource");
  var renderArticleBtn = document.getElementById("renderArticleBtn");
  var securityTestBtn = document.getElementById("securityTestBtn");
  var articleView = document.getElementById("articleView");
  var loaderLogOutput = document.getElementById("loaderLogOutput");

  function appendLogLine(message) {
    var now = new Date();
    var timeText = now.toLocaleTimeString("vi-VN", { hour12: false });
    loaderLogOutput.value += "[" + timeText + "] " + message + "\n";
    loaderLogOutput.scrollTop = loaderLogOutput.scrollHeight;
  }

  function logEvent(eventName, payload) {
    if (payload !== undefined) {
      console.log("[json-loader] " + eventName, payload);
      appendLogLine(eventName + " " + JSON.stringify(payload));
      return;
    }
    console.log("[json-loader] " + eventName);
    appendLogLine(eventName);
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  function sanitizeImageUrl(rawUrl) {
    if (!rawUrl) {
      return "";
    }

    var value = String(rawUrl).trim();
    if (!value) {
      return "";
    }

    var lower = value.toLowerCase();
    if (lower.indexOf("javascript:") === 0 || lower.indexOf("data:") === 0 || lower.indexOf("vbscript:") === 0) {
      return "";
    }

    try {
      var parsed = new URL(value, window.location.href);
      if (parsed.protocol === "http:" || parsed.protocol === "https:") {
        return parsed.href;
      }

      if (parsed.protocol === "file:") {
        return parsed.href;
      }
    } catch (error) {
      return "";
    }

    return "";
  }

  function sanitizePlainText(raw) {
    if (raw === null || raw === undefined) {
      return "";
    }

    var value = String(raw);
    var decoder = document.createElement("textarea");
    decoder.innerHTML = value;
    value = decoder.value;

    value = value.replace(/<[^>]*>/g, "");
    value = value.replace(/javascript\s*:/gi, "");
    value = value.replace(/vbscript\s*:/gi, "");
    value = value.replace(/on[a-z]+\s*=/gi, "");
    value = value.replace(/script/gi, "");
    // Bland text mode: keep only letters, numbers, spaces, and basic punctuation.
    value = value.replace(/[^\p{L}\p{N}\s.,!?;:()\-]/gu, "");
    value = value.replace(/\s+/g, " ");
    return value.trim();
  }

  function sanitizeIncomingData(data) {
    var blocks = Array.isArray(data.blocks) ? data.blocks : [];
    var sanitizedBlocks = [];
    var changed = false;

    blocks.forEach(function (block) {
      if (!block || !block.type || !block.data) {
        changed = true;
        return;
      }

      var nextBlock = {
        type: block.type,
        data: {}
      };

      if (block.type === "header") {
        nextBlock.data.text = sanitizePlainText(block.data.text);
        nextBlock.data.level = Number(block.data.level || 2);
      } else if (block.type === "paragraph") {
        nextBlock.data.text = sanitizePlainText(block.data.text);
      } else if (block.type === "list") {
        var items = Array.isArray(block.data.items) ? block.data.items : [];
        nextBlock.data.style = block.data.style === "ordered" ? "ordered" : "unordered";
        nextBlock.data.items = items.map(function (item) {
          return sanitizePlainText(item);
        });
      } else if (block.type === "quote") {
        nextBlock.data.text = sanitizePlainText(block.data.text);
        nextBlock.data.caption = sanitizePlainText(block.data.caption);
      } else if (block.type === "image") {
        var imageUrl = "";
        if (block.data.file && block.data.file.url) {
          imageUrl = String(block.data.file.url);
        } else if (block.data.url) {
          imageUrl = String(block.data.url);
        }

        imageUrl = sanitizeImageUrl(imageUrl);
        if (!imageUrl) {
          changed = true;
          return;
        }

        nextBlock.data.url = imageUrl;
        nextBlock.data.caption = sanitizePlainText(block.data.caption);
      } else {
        changed = true;
        return;
      }

      if (JSON.stringify(nextBlock.data) !== JSON.stringify(block.data)) {
        changed = true;
      }

      sanitizedBlocks.push(nextBlock);
    });

    return {
      changed: changed,
      data: {
        time: data.time || Date.now(),
        version: data.version || "2.29.1",
        blocks: sanitizedBlocks
      }
    };
  }

  function parseInputJson() {
    var raw = sourceEl.value;
    try {
      var parsed = JSON.parse(raw);
      if (!parsed || !Array.isArray(parsed.blocks)) {
        throw new Error("JSON khong co truong blocks hop le");
      }
      logEvent("parse.success", { blocks: parsed.blocks.length });
      return parsed;
    } catch (error) {
      logEvent("parse.failed", { message: error.message });
      alert("JSON khong hop le. Kiem tra lai format Editor.js");
      return null;
    }
  }

  function renderBlocksToHtml(data) {
    var blocks = Array.isArray(data.blocks) ? data.blocks : [];

    return blocks
      .map(function (block) {
        if (!block || !block.type || !block.data) {
          return "";
        }

        if (block.type === "header") {
          var level = Number(block.data.level || 2);
          if (level < 1 || level > 6) {
            level = 2;
          }
          return "<h" + level + ">" + escapeHtml(block.data.text || "") + "</h" + level + ">";
        }

        if (block.type === "paragraph") {
          return "<p>" + escapeHtml(block.data.text || "") + "</p>";
        }

        if (block.type === "list") {
          var tag = block.data.style === "ordered" ? "ol" : "ul";
          var items = Array.isArray(block.data.items) ? block.data.items : [];
          var htmlItems = items.map(function (item) {
            return "<li>" + escapeHtml(item) + "</li>";
          }).join("");
          return "<" + tag + ">" + htmlItems + "</" + tag + ">";
        }

        if (block.type === "quote") {
          var quote = escapeHtml(block.data.text || "");
          var caption = escapeHtml(block.data.caption || "");
          return "<blockquote><p>" + quote + "</p><cite>" + caption + "</cite></blockquote>";
        }

        if (block.type === "image") {
          var imageUrl = "";
          if (block.data.file && block.data.file.url) {
            imageUrl = String(block.data.file.url);
          } else if (block.data.url) {
            imageUrl = String(block.data.url);
          }

          imageUrl = sanitizeImageUrl(imageUrl);

          if (!imageUrl) {
            logEvent("image.skipped", { reason: "invalid_or_unsafe_url" });
            return "";
          }

          var imageCaption = escapeHtml(block.data.caption || "");
          var altText = imageCaption || "Article image";
          var figure = "<figure><img src=\"" + escapeHtml(imageUrl) + "\" alt=\"" + altText + "\" loading=\"lazy\" />";
          if (imageCaption) {
            figure += "<figcaption>" + imageCaption + "</figcaption>";
          }
          figure += "</figure>";
          return figure;
        }

        return "";
      })
      .join("\n");
  }

  function renderArticleFromJson() {
    var rawData = parseInputJson();
    if (!rawData) {
      return;
    }

    var sanitizeResult = sanitizeIncomingData(rawData);
    var data = sanitizeResult.data;
    if (sanitizeResult.changed) {
      sourceEl.value = JSON.stringify(data, null, 2);
      logEvent("input.sanitized", { message: "Unsafe content was removed from JSON input" });
    }

    var articleHtml = renderBlocksToHtml(data);
    var safeHtml = articleHtml;
    if (typeof DOMPurify !== "undefined") {
      safeHtml = DOMPurify.sanitize(articleHtml, {
        ALLOWED_TAGS: ["h1", "h2", "h3", "h4", "h5", "h6", "p", "ul", "ol", "li", "blockquote", "cite", "figure", "img", "figcaption"],
        ALLOWED_ATTR: ["src", "alt", "loading"],
        ALLOW_DATA_ATTR: false
      });
    } else {
      logEvent("sanitize.warning", { message: "DOMPurify not loaded, using escaped-html only mode" });
    }

    articleView.innerHTML = safeHtml || "<p class=\"editor-lab-muted\">Khong co block hop le de hien thi.</p>";

    logEvent("article.render.success", {
      blocks: Array.isArray(data.blocks) ? data.blocks.length : 0,
      htmlLength: safeHtml.length
    });
  }

  function runSecuritySmokeTest() {
    var payload = {
      time: Date.now(),
      version: "2.29.1",
      blocks: [
        {
          type: "paragraph",
          data: {
            text: "<img src=x onerror=alert(1)> <script>alert('xss')</script> hello"
          }
        },
        {
          type: "image",
          data: {
            url: "javascript:alert(1)",
            caption: "bad image"
          }
        },
        {
          type: "image",
          data: {
            url: "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=800&q=80",
            caption: "safe image"
          }
        }
      ]
    };

    sourceEl.value = JSON.stringify(payload, null, 2);
    renderArticleFromJson();

    var hasScriptTag = Boolean(articleView.querySelector("script"));
    var hasInlineHandler = false;
    var hasJavascriptUrl = false;

    var nodes = articleView.querySelectorAll("*");
    nodes.forEach(function (node) {
      if (!node.attributes) {
        return;
      }

      for (var i = 0; i < node.attributes.length; i += 1) {
        var attr = node.attributes[i];
        var attrName = attr.name.toLowerCase();
        var attrValue = String(attr.value || "").trim().toLowerCase();

        if (attrName.indexOf("on") === 0) {
          hasInlineHandler = true;
        }

        if ((attrName === "src" || attrName === "href") && attrValue.indexOf("javascript:") === 0) {
          hasJavascriptUrl = true;
        }
      }
    });

    var passed = !hasScriptTag && !hasInlineHandler && !hasJavascriptUrl;

    logEvent(passed ? "security.test.pass" : "security.test.fail", {
      hasScriptTag: hasScriptTag,
      hasInlineHandler: hasInlineHandler,
      hasJavascriptUrl: hasJavascriptUrl
    });

    if (!passed) {
      alert("Security test FAILED. Xem logs de debug them.");
      return;
    }

    alert("Security test PASSED. Script/handler/javascript URL da bi chan.");
  }

  renderArticleBtn.addEventListener("click", renderArticleFromJson);
  securityTestBtn.addEventListener("click", runSecuritySmokeTest);

  renderArticleFromJson();
  logEvent("page.loaded");
})();
