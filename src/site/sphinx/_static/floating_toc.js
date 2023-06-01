// JavaScript code for creating the floating TOC
window.addEventListener('DOMContentLoaded', function() {
  var tocContainer = document.getElementById('floating-toc');
  var showBtn = document.getElementById('toc-hide-show-btn');
  var tocList = document.getElementById('toc-list');
  var headings = document.querySelectorAll('h1, h2, h3');
  var tocLevels = [0, 0, 0];

  // Calculate the initial position of the TOC
  const tocContainerRect = tocContainer.getBoundingClientRect();
  const tocContainerRight = tocContainer.style.right;
  const buttonText = document.getElementById('buttonText');

  headings.forEach(function(heading) {
    var level = parseInt(heading.tagName.substr(1), 10) - 1;

    tocLevels[level]++;
    for (var i = level + 1; i < 3; i++) {
      tocLevels[i] = 0;
    }

    var listItem = document.createElement('li');
    var link = document.createElement('a');

    var number = tocLevels.slice(0, level + 1).join('.') + ' ';
    link.textContent = number + heading.textContent.trim().replace(/#$/, '').replace(/¶$/, '');

    var headingId = 'heading-' + Math.random().toString(36).substr(2, 9);
    heading.setAttribute('id', headingId);
    link.href = '#' + headingId;

    var styledHeading = document.createElement('h' + (level + 1));
    styledHeading.appendChild(link);
    listItem.appendChild(styledHeading);

    tocList.appendChild(listItem);
  });

  // Toggle TOC visibility
  showBtn.addEventListener('click', function() {
    if (tocContainer.style.right != tocContainerRight) {
      tocContainer.style.right = tocContainerRight;
      // buttonText.innerText="H";
    } else {
      tocContainer.style.right = `-${tocContainerRect.width-26}px`;
      // buttonText.innerText="S";
    };
  });

  // JavaScript code for searching the TOC
  var searchInput = document.getElementById('toc-search');
  var tocItems = Array.from(tocList.getElementsByTagName('li'));

  searchInput.addEventListener('input', function() {
    var searchValue = this.value.toLowerCase();

    tocItems.forEach(function(item) {
      var link = item.querySelector('a');
      var linkText = link.textContent.toLowerCase();

      if (linkText.includes(searchValue)) {
        item.style.display = 'block';
      } else {
        item.style.display = 'none';
      }
    });
  });

  // JavaScript code for updating the floating TOC on scroll
  window.addEventListener('scroll', function() {
    var scrollPosition = window.pageYOffset || document.documentElement.scrollTop;

    var visibleHeading = null;
    headings.forEach(function(heading) {
      var rect = heading.getBoundingClientRect();
      if (rect.top > 0 && rect.top < window.innerHeight) {
        visibleHeading = heading;
        return;
      }
    });

    if (visibleHeading) {
      var activeLink = tocList.querySelector('a[href="#' + visibleHeading.id + '"]');
      if (activeLink) {
        activeLink.classList.add('active');
        tocContainer.scrollTop = activeLink.offsetTop - tocContainer.offsetTop;
      }

      // Remove 'active' class from other links
      var allLinks = tocList.querySelectorAll('a');
      allLinks.forEach(function(link) {
        if (link !== activeLink) {
          link.classList.remove('active');
        }
      });
    }
  });
});
