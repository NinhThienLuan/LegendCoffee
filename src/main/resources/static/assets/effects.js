(function initLandingEffects() {
  if (!window.gsap || !window.ScrollTrigger) {
    return;
  }

  const stackTop = 280;
  let lenis = null;

  if (window.Lenis) {
    lenis = new Lenis({
      duration: 1.2,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smoothWheel: true,
      smoothTouch: false
    });

    function raf(time) {
      lenis.raf(time);
      requestAnimationFrame(raf);
    }
    requestAnimationFrame(raf);
  }

  gsap.registerPlugin(ScrollTrigger);
  if (lenis) {
    lenis.on("scroll", () => ScrollTrigger.update());
  }

  initRevealWords(stackTop);
  initStackCards(stackTop);
  ScrollTrigger.refresh();
})();

function initRevealWords(stackTop) {
  const revealTextElements = document.querySelectorAll(".scroll-reveal-text");

  revealTextElements.forEach((el) => {
    const words = el.innerText.split(/(\s+)/);
    el.innerHTML = "";

    words.forEach((word) => {
      const span = document.createElement("span");
      span.className = "reveal-word";
      span.innerText = word;
      el.appendChild(span);
    });

    const spans = el.querySelectorAll(".reveal-word");
    const parentCard = el.closest(".stack-card");

    gsap.fromTo(
      spans,
      { opacity: 0.1, filter: "blur(4px)", rotate: 3 },
      {
        opacity: 1,
        filter: "blur(0px)",
        rotate: 0,
        stagger: parentCard ? 0.05 : 0.1,
        scrollTrigger: {
          trigger: parentCard || el,
          start: parentCard ? "top bottom" : "top 85%",
          end: parentCard ? `top ${stackTop}px` : "top 40%",
          scrub: 1,
          toggleActions: "play none none reverse"
        }
      }
    );
  });
}

function initStackCards(stackTop) {
  const stackCards = document.querySelectorAll(".stack-card");

  stackCards.forEach((card, index) => {
    card.style.top = `${stackTop}px`;
    if (index === 0) {
      return;
    }

    gsap.to(stackCards[index - 1], {
      scale: 0.95,
      opacity: 0.5,
      filter: "blur(6px)",
      scrollTrigger: {
        trigger: card,
        start: `top ${stackTop + 100}px`,
        end: `top ${stackTop}px`,
        scrub: true
      }
    });
  });
}
