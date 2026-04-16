# New Page Integration Checklist

Use this checklist whenever you add a new HTML page to the project to ensure consistency and maintainability.

## ✅ Shared Fragments & Structure
- [ ] Use the shared head fragment at the top:
  `<head th:replace="~{fragments/head :: sharedHead('Your Page Title')}"></head>`
- [ ] Add the navbar fragment right after `<body>`:
  `<div th:replace="~{fragments/navbar :: navbar(${navItems})}"></div>`
- [ ] Add the footer fragment before `</body>`:
  `<div th:replace="~{fragments/footer :: footer}"></div>`

## ✅ Styles & Scripts
- [ ] Do NOT copy/paste Tailwind config, fonts, or animation scripts—these are already included in the shared head fragment.
- [ ] Use the standard body class for theming:
  `bg-surface text-on-surface selection:bg-primary-container selection:text-on-primary-container`
- [ ] If you need new global styles, update `static/assets/app.css` or the shared head fragment.

## ✅ Content Fragments
- [ ] Use content fragments (e.g., `fragments/catalogs`, `fragments/sections`) as needed for your page type.
- [ ] Avoid duplicating HTML for shared sections—always use fragments.

## ✅ General
- [ ] Test your page for consistent look and feel with existing pages.
- [ ] Check that all links, scripts, and styles are loaded only once (no duplicates).
- [ ] If you add new shared UI or scripts, consider updating the shared fragments.

---
_Keep this checklist up to date as the project evolves!_
