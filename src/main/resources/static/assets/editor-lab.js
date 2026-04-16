(function () {
  if (typeof EditorJS === "undefined") {
    console.error("[editor-lab] EditorJS is not loaded from CDN");
    alert("Khong tai duoc EditorJS tu CDN. Kiem tra mang hoac refresh trang.");
    return;
  }

  var editor;
  var saveBtn = document.getElementById("saveBtn");
  var resetBtn = document.getElementById("resetBtn");

  var jsonInput = document.getElementById("jsonData");
  var jsonOutput = document.getElementById("jsonOutput");
  var logOutput = document.getElementById("logOutput");

  function normalizeSafeImageUrl(rawUrl) {
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
      if (parsed.protocol === "http:" || parsed.protocol === "https:" || parsed.protocol === "file:") {
        return parsed.href;
      }
    } catch (error) {
      return "";
    }

    return "";
  }

  function UrlImageTool(config) {
    this.data = config.data || {};
    this.wrapper = null;
    this.urlInput = null;
    this.captionInput = null;
  }

  UrlImageTool.prototype.render = function () {
    var wrapper = document.createElement("div");
    wrapper.style.display = "grid";
    wrapper.style.gap = "8px";

    var urlInput = document.createElement("input");
    urlInput.type = "url";
    urlInput.placeholder = "Dan image URL (https://...)";
    urlInput.value = this.data.url || "";
    urlInput.style.width = "100%";
    urlInput.style.border = "1px solid rgba(130,116,113,0.24)";
    urlInput.style.borderRadius = "8px";
    urlInput.style.padding = "10px";

    var captionInput = document.createElement("input");
    captionInput.type = "text";
    captionInput.placeholder = "Caption (optional)";
    captionInput.value = this.data.caption || "";
    captionInput.style.width = "100%";
    captionInput.style.border = "1px solid rgba(130,116,113,0.24)";
    captionInput.style.borderRadius = "8px";
    captionInput.style.padding = "10px";

    var preview = document.createElement("img");
    preview.alt = "Image preview";
    preview.loading = "lazy";
    preview.style.maxHeight = "260px";
    preview.style.width = "100%";
    preview.style.objectFit = "cover";
    preview.style.borderRadius = "8px";
    var initialSafeUrl = normalizeSafeImageUrl(urlInput.value);
    preview.style.display = initialSafeUrl ? "block" : "none";
    preview.src = initialSafeUrl;

    urlInput.addEventListener("input", function () {
      var safeUrl = normalizeSafeImageUrl(urlInput.value);
      var hasUrl = Boolean(safeUrl);
      preview.style.display = hasUrl ? "block" : "none";
      preview.src = hasUrl ? safeUrl : "";
    });

    wrapper.appendChild(urlInput);
    wrapper.appendChild(captionInput);
    wrapper.appendChild(preview);

    this.wrapper = wrapper;
    this.urlInput = urlInput;
    this.captionInput = captionInput;

    return wrapper;
  };

  UrlImageTool.prototype.save = function () {
    var safeUrl = normalizeSafeImageUrl(this.urlInput ? this.urlInput.value : "");
    return {
      url: safeUrl,
      caption: this.captionInput ? this.captionInput.value.trim() : ""
    };
  };

  UrlImageTool.toolbox = {
    title: "Image URL",
    icon: '<svg width="17" height="17" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M5 5H19V19H5V5Z" stroke="currentColor" stroke-width="1.8"/><path d="M8 15L11 12L13.5 14.5L16 12L19 15" stroke="currentColor" stroke-width="1.8"/><circle cx="9" cy="9" r="1.6" fill="currentColor"/></svg>'
  };

  function appendLogLine(message) {
    var now = new Date();
    var timeText = now.toLocaleTimeString("vi-VN", { hour12: false });
    logOutput.value += "[" + timeText + "] " + message + "\n";
    logOutput.scrollTop = logOutput.scrollHeight;
  }

  function logEvent(eventName, payload) {
    if (payload !== undefined) {
      console.log("[editor-lab] " + eventName, payload);
      appendLogLine(eventName + " " + JSON.stringify(payload));
      return;
    }
    console.log("[editor-lab] " + eventName);
    appendLogLine(eventName);
  }

  function safeParseJson(raw) {
    if (!raw) {
      logEvent("safeParseJson.empty_input");
      return { time: Date.now(), blocks: [], version: "2.29.1" };
    }
    try {
      var parsed = JSON.parse(raw);
      logEvent("safeParseJson.success", { blocks: Array.isArray(parsed.blocks) ? parsed.blocks.length : 0 });
      return parsed;
    } catch (error) {
      console.error("Cannot parse JSON data", error);
      logEvent("safeParseJson.fallback_default", { reason: "invalid_json" });
      return { time: Date.now(), blocks: [], version: "2.29.1" };
    }
  }

  function setJsonOutput(data, serialized) {
    jsonInput.value = serialized;
    jsonOutput.value = JSON.stringify(data, null, 2);
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

  function sanitizeEditorData(data) {
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
      } else if (block.type === "quote") {
        nextBlock.data.text = sanitizePlainText(block.data.text);
        nextBlock.data.caption = sanitizePlainText(block.data.caption);
      } else if (block.type === "list") {
        var items = Array.isArray(block.data.items) ? block.data.items : [];
        nextBlock.data.style = block.data.style === "ordered" ? "ordered" : "unordered";
        nextBlock.data.items = items.map(function (item) {
          return sanitizePlainText(item);
        });
      } else if (block.type === "image") {
        var rawUrl = block.data.url || (block.data.file && block.data.file.url) || "";
        var safeUrl = normalizeSafeImageUrl(rawUrl);
        if (!safeUrl) {
          changed = true;
          return;
        }
        nextBlock.data.url = safeUrl;
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

  async function saveEditorData() {
    try {
      var rawData = await editor.save();
      var sanitizedResult = sanitizeEditorData(rawData);
      var data = sanitizedResult.data;
      var serialized = JSON.stringify(data);
      setJsonOutput(data, serialized);

      if (sanitizedResult.changed) {
        logEvent("input.sanitized", { message: "Unsafe html/script patterns were removed from input" });
      }

      logEvent("save.success", {
        blocks: Array.isArray(data.blocks) ? data.blocks.length : 0,
        version: data.version || "n/a"
      });
    } catch (error) {
      console.error("Save failed", error);
      logEvent("save.failed", {
        message: error && error.message ? error.message : "unknown_error"
      });
      alert("Khong luu duoc du lieu editor. Kiem tra console de xem chi tiet.");
    }
  }

  async function resetWithSample() {
    var sample = {
      time: Date.now(),
      version: "2.29.1",
      blocks: [
        {
          type: "header",
          data: {
            text: "Hoc Spring Boot co ban",
            level: 2
          }
        },
        {
          type: "paragraph",
          data: {
            text: "Day la noi dung bai viet dau tien..."
          }
        },
        {
          type: "list",
          data: {
            style: "unordered",
            items: ["Luu content_json", "Render content_html", "Edit lai tu JSON"]
          }
        },
        {
          type: "image",
          data: {
            url: "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=80",
            caption: "Anh minh hoa bai viet"
          }
        }
      ]
    };

    await editor.render(sample);
    var serialized = JSON.stringify(sample);
    setJsonOutput(sample, serialized);
    logEvent("reset.sample_loaded", { blocks: sample.blocks.length });
  }

  function initEditor() {
    var initialData = safeParseJson(jsonInput.value);
    logEvent("init.start", { initialBlocks: Array.isArray(initialData.blocks) ? initialData.blocks.length : 0 });

    var HeaderTool = typeof Header !== "undefined" ? Header : null;
    var ParagraphTool = typeof Paragraph !== "undefined" ? Paragraph : null;
    var ListTool = typeof EditorjsList !== "undefined" ? EditorjsList : (typeof List !== "undefined" ? List : null);
    var QuoteTool = typeof Quote !== "undefined" ? Quote : null;

    var tools = {};

    if (HeaderTool) {
      tools.header = {
        class: HeaderTool,
        inlineToolbar: ["link"],
        config: {
          placeholder: "Nhap tieu de"
        }
      };
    }

    if (ParagraphTool) {
      tools.paragraph = {
        class: ParagraphTool,
        inlineToolbar: true
      };
    } else {
      logEvent("init.warning", { message: "Paragraph tool missing. Falling back to default paragraph." });
    }

    if (ListTool) {
      tools.list = {
        class: ListTool,
        inlineToolbar: true
      };
    }

    if (QuoteTool) {
      tools.quote = {
        class: QuoteTool,
        inlineToolbar: true,
        config: {
          quotePlaceholder: "Nhap noi dung quote",
          captionPlaceholder: "Nguon"
        }
      };
    }

    tools.image = {
      class: UrlImageTool
    };

    try {
      editor = new EditorJS({
        holder: "editorjs",
        data: initialData,
        autofocus: true,
        tools: tools,
        onReady: function () {
          var serialized = JSON.stringify(initialData);
          setJsonOutput(initialData, serialized);
          logEvent("init.ready");
        }
      });

      if (editor && editor.isReady && typeof editor.isReady.then === "function") {
        editor.isReady.then(function () {
          logEvent("init.isReady.resolved");
        }).catch(function (error) {
          logEvent("init.isReady.rejected", { message: error && error.message ? error.message : "unknown_error" });
          console.error("[editor-lab] Editor isReady rejected", error);
        });
      }
    } catch (error) {
      logEvent("init.failed", { message: error && error.message ? error.message : "unknown_error" });
      console.error("[editor-lab] init failed", error);
      alert("Khoi tao editor that bai. Mo console de xem chi tiet loi.");
    }
  }

  saveBtn.addEventListener("click", saveEditorData);
  resetBtn.addEventListener("click", resetWithSample);

  initEditor();
  logEvent("page.loaded");
})();
