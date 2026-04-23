(function initSmoothScroll() {
  if (window.Lenis) {
    const lenis = new Lenis({
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

    // Optional: Connect to ScrollTrigger if it exists
    if (window.gsap && window.ScrollTrigger) {
      lenis.on("scroll", () => ScrollTrigger.update());
      gsap.registerPlugin(ScrollTrigger);
    }
  }
})();
