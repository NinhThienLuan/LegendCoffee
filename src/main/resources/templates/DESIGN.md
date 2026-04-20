```markdown
# Design System Document: Industrial Excellence

## 1. Overview & Creative North Star: "The Industrial Architect"
The vision for this design system is **"The Industrial Architect."** We are moving away from the "cozy neighborhood cafe" trope and leaning into the authoritative, large-scale world of B2B coffee commerce. This is about the precision of roasting, the scale of logistics, and the reliability of a global supply chain.

To achieve a "High-End Editorial" feel, the system rejects standard templated layouts in favor of **Structural Asymmetry**. We utilize generous white space (the "Cream" palette) to frame high-contrast industrial photography. Typography is used as a graphic element—large, bold headlines that command attention, paired with a utilitarian grid that feels as organized as a modern distribution center.

## 2. Colors: Tonal Depth & The "No-Line" Rule
This palette is rooted in the earth and the machine. It uses rich, dark browns (`primary`) and muted, sophisticated neutrals to create a sense of grounded reliability.

### The "No-Line" Rule
**Strict Mandate:** Designers are prohibited from using 1-pixel solid borders to define sections. Layout boundaries must be defined solely through background color shifts.
- To separate a section, transition from `surface` (#faf9f5) to `surface-container-low` (#f4f4f0).
- This creates a seamless, "molded" look that feels more expensive than a boxed-in layout.

### Surface Hierarchy & Nesting
Treat the interface as a series of physical layers. Use the `surface-container` tiers to create depth:
- **Level 0 (Base):** `surface` or `background` for the main canvas.
- **Level 1 (Sections):** `surface-container-low` for large content blocks.
- **Level 2 (Cards/Modules):** `surface-container-lowest` (#ffffff) to make elements "pop" forward without shadows.

### Glass & Texture
- **Glassmorphism:** For floating navigation or modal overlays, use `surface` at 80% opacity with a `24px` backdrop blur. This allows the rich coffee tones of the imagery to bleed through softly.
- **Signature Gradients:** For primary CTAs and hero backgrounds, use a subtle linear gradient from `primary` (#351f1b) to `primary_container` (#4d3430) at a 135-degree angle. This adds "soul" and weight to the industrial theme.

## 3. Typography: The Authority of Manrope & Work Sans
We use a dual-font system to balance modern engineering with human readability.

- **Display & Headlines (Manrope):** A geometric sans-serif that feels engineered and reliable. 
    - Use `display-lg` (3.5rem) for bold statements like "CUNG CẤP CÀ PHÊ SỐ LƯỢNG LỚN" (Bulk Coffee Supply).
    - Tighten letter spacing by -2% on headlines to increase the "premium" feel.
- **Body & Labels (Work Sans):** Chosen for its exceptional legibility in technical specs and price lists.
    - **Body-lg:** Used for descriptions of roasting processes.
    - **Label-md:** Used for technical data like "Độ ẩm: <12.5%" or "Tỷ lệ hạt đen: 0.1%."

**Typography as Branding:** Use `on_surface_variant` (#504442) for secondary text to reduce visual noise while maintaining a "warm" industrial tone.

## 4. Elevation & Depth: Tonal Layering
Traditional drop shadows are largely replaced by **Tonal Layering**.

- **The Layering Principle:** Instead of a shadow, place a `surface-container-lowest` card on a `surface-container-low` background. The slight shift in brightness provides all the hierarchy needed.
- **Ambient Shadows:** If a floating element (like a "Yêu cầu báo giá" / Request Quote FAB) is required, use a shadow with a 32px blur, 0px offset, and 6% opacity, using the `primary` color as the shadow tint.
- **The "Ghost Border":** For input fields, use the `outline_variant` (#d4c3bf) at 30% opacity. It should be felt, not seen.

## 5. Components: Precision & Utility

### Buttons (Nút)
- **Primary:** Roundedness `md` (0.375rem). Background: `primary` (#351f1b). Text: `on_primary` (#ffffff). No border.
- **Secondary:** `surface-container-highest` background with `on_surface` text.
- **Interaction:** On hover, primary buttons should shift to `primary_container`.

### Cards & Lists (Thẻ & Danh sách)
- **NO DIVIDERS:** Do not use horizontal lines between list items. Use 24px of vertical padding and a background shift on hover (`surface-container-low`).
- **B2B Bulk Pricing Tables:** Use `surface-container-lowest` for the table header and alternating `surface` and `surface-container-low` for rows.

### Inputs (Ô nhập liệu)
- Background should be `surface-container-low`. 
- Focused state: A "Ghost Border" of `primary` at 40% and a subtle 2px bottom-bar in `primary`.

### Industry-Specific Components
- **Spec Sheets:** High-density info modules using `label-sm` for "Machine Specs" or "Bean Origin" data, set against `surface-variant`.
- **Order Progress Tracker:** A thick, 4px track using `secondary_container` with `primary` as the active state, signifying the "Roast to Delivery" pipeline.

## 6. Do's and Don'ts

### Do:
- **Use Large-Scale Imagery:** Show the industrial roaster, the warehouse, and the 60kg burlap sacks. This is about B2B scale.
- **Embrace Asymmetry:** Align text to the left but place a large bean-texture image partially off-screen to the right.
- **Localize Currency:** Always display prices in VND (e.g., 500.000 ₫) using `title-lg` in `primary` color to denote value.

### Don't:
- **No Cafe Aesthetics:** Avoid photos of latte art, cozy wooden chairs, or people relaxing. This design system is for the *producers* and *purchasers*, not the end consumer.
- **No Sharp Corners:** While industrial, we aren't "brutalist." Use the `DEFAULT` (0.25rem) or `md` (0.375rem) roundedness to keep the brand approachable and professional.
- **No High-Contrast Borders:** Never use a #000000 or high-opacity `outline` for boxes. It breaks the sophisticated tonal flow.

---
**Director's Final Note:** This design system is a tool for building trust through professional aesthetics. Every pixel should feel intentional, every margin should feel spacious, and every color should remind the user of the rich, industrial heritage of premium coffee production.```