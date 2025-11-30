/**
 * Sidebar Toggle Functionality
 * Handles collapse/expand of sidebar in desktop and mobile views
 */

document.addEventListener("DOMContentLoaded", function () {
  const sidebar = document.getElementById("sidebarMenu");
  const sidebarToggle = document.getElementById("sidebarToggle");
  const sidebarBackdrop = document.getElementById("sidebarBackdrop");
  const mainContent = document.querySelector("main");

  // Load saved state from localStorage
  const savedState = localStorage.getItem("sidebarCollapsed");
  if (savedState === "true") {
    sidebar.classList.add("collapsed");
  }

  // Toggle sidebar on button click
  if (sidebarToggle) {
    sidebarToggle.addEventListener("click", function () {
      const isCollapsed = sidebar.classList.toggle("collapsed");

      // Save state
      localStorage.setItem("sidebarCollapsed", isCollapsed);

      // Update icon
      const icon = this.querySelector("i");
      if (icon) {
        icon.className = isCollapsed
          ? "bi bi-chevron-right"
          : "bi bi-chevron-left";
      }
    });
  }

  // Mobile: Show/hide sidebar
  const mobileToggle = document.getElementById("sidebarMobileToggle");
  if (mobileToggle) {
    mobileToggle.addEventListener("click", function () {
      sidebar.classList.toggle("show");
      if (sidebarBackdrop) {
        sidebarBackdrop.classList.toggle("show");
      }
    });
  }

  // Close sidebar when clicking backdrop
  if (sidebarBackdrop) {
    sidebarBackdrop.addEventListener("click", function () {
      sidebar.classList.remove("show");
      this.classList.remove("show");
    });
  }

  // Close sidebar when clicking a link on mobile
  const sidebarLinks = sidebar.querySelectorAll(".nav-link");
  sidebarLinks.forEach((link) => {
    link.addEventListener("click", function () {
      if (window.innerWidth < 768) {
        sidebar.classList.remove("show");
        if (sidebarBackdrop) {
          sidebarBackdrop.classList.remove("show");
        }
      }
    });
  });
});
