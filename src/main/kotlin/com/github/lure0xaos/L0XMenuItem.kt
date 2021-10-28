package com.github.lure0xaos

import java.util.*

@Suppress("ConvertSecondaryConstructorToPrimary", "MemberVisibilityCanBePrivate")
class L0XMenuItem : Comparable<L0XMenuItem> {

    companion object {
        fun buildMenu(structure: List<Pair<String, String>>, topics: List<Map<String, String>>): Set<L0XMenuItem> =
            buildMenu(structure, topics, null)

        fun buildMenu(
            hierarchy: List<Pair<String, String>>,
            topics: List<Map<String, String>>,
            selected: Map<String, String>?,
        ): Set<L0XMenuItem> {
            val roots: MutableSet<L0XMenuItem> = TreeSet()
            topics.forEach { topic: Map<String, String> ->
                var item: L0XMenuItem =
                    createRoot(roots, topic.getValue(hierarchy[0].second), topic.getValue(hierarchy[0].first))
                hierarchy.forEachIndexed { index, (alias, text) ->
                    if (index != 0 && index != hierarchy.lastIndex) {
                        item = item.assignChildNode(topic.getValue(text), topic.getValue(alias))
                    }
                }
                item = item.assignChildLeaf(
                    topic.getValue(hierarchy[hierarchy.lastIndex].second),
                    topic.getValue(hierarchy[hierarchy.lastIndex].first),
                    topic[L0X.KEY_TOPIC_FILE]
                )
                if (topic == selected) item.show()
            }
            return Collections.unmodifiableSet(roots)
        }

        fun buildBreadcrumb(hierarchy: List<Pair<String, String>>, topic: Map<String, String>): List<L0XMenuItem> {
            val list: MutableList<L0XMenuItem> = mutableListOf()
            hierarchy.forEachIndexed { index, (alias, text) ->
                list +=
                    if (index == hierarchy.lastIndex)
                        createLeaf(topic.getValue(text), topic.getValue(alias), topic[L0X.KEY_TOPIC_FILE])
                    else
                        createNode(topic.getValue(text), topic.getValue(alias))
            }
            return list
        }

        private fun createRoot(roots: MutableSet<L0XMenuItem>, text: String, alias: String): L0XMenuItem {
            val item =
                L0XMenuItem(parent = null, text = text, alias = alias, leaf = false, visible = false, file = null)
            for (root: L0XMenuItem in roots) if (root == item) return root
            roots += item
            return item
        }

        private fun createNode(text: String, alias: String): L0XMenuItem =
            L0XMenuItem(parent = null, text = text, alias = alias, leaf = false, visible = true, file = null)

        private fun createLeaf(text: String, alias: String, file: String?): L0XMenuItem =
            L0XMenuItem(parent = null, text = text, alias = alias, leaf = true, visible = true, file = file)
    }

    var parent: L0XMenuItem?
    val label: String
    val title: String
    val alias: String?
    val leaf: Boolean
    var visible: Boolean
    val id: String
    val children: MutableSet<L0XMenuItem> = TreeSet<L0XMenuItem>()
    var file: String?

    private constructor (
        parent: L0XMenuItem?,
        text: String,
        alias: String,
        leaf: Boolean,
        visible: Boolean,
        file: String?,
    ) {
        this.parent = parent
        this.label = text
        this.title = text
        this.alias = if (leaf) alias else null
        this.leaf = leaf
        this.visible = visible
        this.id = if (null == parent) alias else "${parent.id}_$alias"
        this.file = file
    }

    private fun assignChildNode(text: String, alias: String): L0XMenuItem =
        assignChild(L0XMenuItem(parent = this, text = text, alias = alias, leaf = false, visible = false, file = null))

    private fun assignChildLeaf(text: String, alias: String, file: String?): L0XMenuItem =
        assignChild(L0XMenuItem(parent = this, text = text, alias = alias, leaf = true, visible = false, file = file))

    private fun assignChild(item: L0XMenuItem): L0XMenuItem {
        for (child: L0XMenuItem in children)
            if (child == item)
                return child
        item.parent = this
        children.add(item)
        return item
    }

    private fun show() {
        var item: L0XMenuItem? = this
        while (null != item) {
            item.visible = true
            item = item.parent
        }
    }

    override fun compareTo(other: L0XMenuItem): Int = String.CASE_INSENSITIVE_ORDER.compare(id, other.id)

    override fun toString(): String = "MenuItem(id=${id}, label=${label}, alias=${alias})"

    override fun equals(other: Any?): Boolean =
        if (this === other) true else if (javaClass != other?.javaClass) false else id == (other as L0XMenuItem).id

    override fun hashCode(): Int = id.hashCode()
}
