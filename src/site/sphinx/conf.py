# -*- coding: utf-8 -*-
import os
import sys

sys.path.insert(0, os.path.abspath(".."))

# General options
needs_sphinx = "7.2"
add_function_parentheses = True

extensions = [
    "myst_parser",
    "sphinx.ext.autodoc",
    "sphinx.ext.autosectionlabel",
    "sphinx.ext.extlinks",
    "sphinx_substitution_extensions",
    "sphinx_inline_tabs",
    "pygments.sphinxext",
    "sphinx_javadoc_xml",
]


source_encoding = "utf-8-sig"
# pygments_style = 'friendly'
show_sphinx = False
master_doc = "index"
exclude_patterns = ["_themes", "_static/css"]
logo_only = True

# HTML options
html_theme = "manticore_sphinx_theme"
html_short_title = "JSQLParser"
htmlhelp_basename = "JSQLParser" + "-doc"
html_use_index = True
html_show_sourcelink = False
html_static_path = ["_static"]
html_logo = "logo-no-background.svg"
html_favicon = "favicon.svg"
html_css_files = ["svg.css"]

html_theme_options = {
    "logo": "logo-no-background.svg",
    "logo_alt": "JSQL Parser",
    "favicon": "favicon.svg",
    "color_primary": "#0063db",
    "color_accent": "#d90000",
    "color_sidebar_bg": "#f5f6fa",
    "color_sidebar_text": "#2d2d48",
    "navigation_depth": 1,
    "show_breadcrumbs": True,
    "footer_text": "All rights reserved.",
    "show_powered_by": True,
    "repo_url": "https://github.com/JSQLParser/JSqlParser",
    "repo_name": "GitHub",
    "landing_page": "",
    "collapse_navigation": True,
}
