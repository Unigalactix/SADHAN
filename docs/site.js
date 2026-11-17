"use strict";

const filters = document.querySelector(".tool-filters");
const cards = [...document.querySelectorAll(".tool-card")];
const filterStatus = document.querySelector("#filter-status");
filters.hidden = false;
filters.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-filter]");
  if (!button) return;
  const category = button.dataset.filter;
  let count = 0;
  cards.forEach((card) => {
    card.hidden = category !== "all" && card.dataset.category !== category;
    if (!card.hidden) count += 1;
  });
  filters.querySelectorAll("button").forEach((item) => {
    const active = item === button;
    item.classList.toggle("active", active);
    item.setAttribute("aria-pressed", String(active));
  });
  filterStatus.textContent = `Showing ${count} ${category === "all" ? "" : `${category} `}tools`;
});

async function loadRelease() {
  const status = document.querySelector("#release-status");
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 8000);
  try {
    const response = await fetch("https://api.github.com/repos/Unigalactix/SADHAN/releases/latest", {
      headers: { Accept: "application/vnd.github+json" },
      signal: controller.signal,
    });
    if (response.status === 404) {
      status.textContent = "First release is being prepared · Android 10+ · Free & open source";
      return;
    }
    if (!response.ok) throw new Error(`GitHub returned HTTP ${response.status}`);
    const release = await response.json();
    if (!/^v\d+\.\d+\.\d+$/.test(release.tag_name) || !Array.isArray(release.assets)) {
      throw new Error("Unexpected release metadata");
    }
    const apk = release.assets.find((asset) =>
      asset.name === `SADHAN-${release.tag_name}.apk` &&
      typeof asset.browser_download_url === "string" &&
      asset.browser_download_url.startsWith("https://github.com/Unigalactix/SADHAN/releases/download/"),
    );
    if (!apk) throw new Error("Release APK is not attached yet");
    document.querySelectorAll("[data-download]").forEach((link) => {
      link.href = apk.browser_download_url;
    });
    const size = Number.isFinite(apk.size) ? ` · ${(apk.size / 1048576).toFixed(1)} MB` : "";
    status.textContent = `${release.tag_name}${size} · Android 10+ · Signed APK`;
  } catch (error) {
    status.textContent = "Android 10+ · View available downloads on GitHub Releases";
    console.warn("SADHAN release lookup unavailable:", error.message);
  } finally {
    clearTimeout(timeout);
  }
}

loadRelease();
