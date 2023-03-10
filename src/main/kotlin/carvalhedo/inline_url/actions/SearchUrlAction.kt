package carvalhedo.inline_url.actions

import carvalhedo.inline_url.services.TogglePersistence
import clojure.java.api.Clojure
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import carvalhedo.inline_url.util.ClojureUtil.SEARCH_URL_NS
import carvalhedo.inline_url.util.ClojureUtil.requireClojure
import com.intellij.openapi.components.service


class SearchUrlAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val oldLoader = Thread.currentThread().contextClassLoader
        try {
            val loader = SearchUrlAction::class.java.classLoader
            Thread.currentThread().contextClassLoader = loader

            requireClojure(SEARCH_URL_NS)

            val vFile = e.getData(PlatformDataKeys.VIRTUAL_FILE)
            val fileName: String = vFile?.name as String

            if(Clojure.`var`(SEARCH_URL_NS, "is-edn?").invoke(fileName) as Boolean) {
                val path = service<TogglePersistence>().state.path
                if (path.isEmpty()) {
                    Messages.showMessageDialog(e.project, "The path to the urls is not set yet", "Error", Messages.getInformationIcon())
                } else {
                    val fileContent: String = e.project?.let { FileEditorManager.getInstance(it).selectedTextEditor?.document?.text } as String
                    val url: String? = Messages.showInputDialog(e.project, "Url:", "Search Your Url", Messages.getQuestionIcon())
                    if (url != null) {
                        val handlers: String = Clojure.`var`(SEARCH_URL_NS, "search-url").invoke(fileContent, path, url) as String
                        Messages.showMessageDialog(e.project, handlers, "Handlers For The Url", Messages.getInformationIcon())
                    }
                }
            } else {
                Messages.showMessageDialog(e.project, "Wrong file type.\nPlease try again in a *.edn file", "Error", Messages.getInformationIcon())
            }
        } finally {
            Thread.currentThread().contextClassLoader = oldLoader
        }
    }
}