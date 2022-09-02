# -*- coding: utf-8 -*-
import sys, os
#import sphinx_rtd_theme


# General options
needs_sphinx = '1.0'
add_function_parentheses = True

extensions = ['myst_parser', 'sphinx.ext.autodoc', 'sphinx.ext.autosectionlabel', 'sphinx.ext.extlinks', 'sphinx-prompt', 'sphinx_substitution_extensions', 'sphinx_issues', 'sphinx_tabs.tabs', 'pygments.sphinxext', ]

issues_github_path = "JSQLParser/JSqlParser"

#templates_path = ['templates']
source_encoding = 'utf-8-sig'
pygments_style = 'friendly'
show_sphinx = False
master_doc = 'index'
exclude_patterns = ['_themes', '_static/css']
#sphinx_tabs_disable_css_loading = True


# HTML options
html_theme = "manticore_sphinx_theme"
html_theme_path = ["_themes"]
html_short_title = "JSQLParser"
htmlhelp_basename = "JSQLParser" + '-doc'
html_use_index = True
html_show_sourcelink = False
html_static_path = ['_static']
html_logo = '_static/manticore_logo.png'
html_css_files = ["css/theme.css"]


html_theme_options = {
    'canonical_url': '',
    'analytics_id': 'UA-XXXXXXX-1',
    'style_external_links': True,
    'collapse_navigation': True,
    'sticky_navigation': True,
    'navigation_depth': 4,
    'includehidden': True,
    'titles_only': False,
}


html_context = {
    'landing_page': {
        'menu': [
            {'title': 'Online Demo', 'url': 'http://217.160.215.75:8080/jsqlformatter/demo.html'},
            {'title': 'Issue Tracker', 'url': 'https://github.com/JSQLParser/JSqlParser/issues'}
        ]
    }
}

# PlantUML options
plantuml = os.getenv('plantuml')

