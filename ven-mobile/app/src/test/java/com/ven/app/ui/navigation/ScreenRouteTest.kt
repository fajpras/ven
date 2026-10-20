package com.ven.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenRouteTest {

    @Test
    fun screenRoutes_createPost_isCorrect() {
        assertEquals("create_post", Screen.CreatePost.route)
    }

    @Test
    fun screenRoutes_createExhibit_isCorrect() {
        assertEquals("create_exhibit", Screen.CreateExhibit.route)
    }

    @Test
    fun screenRoutes_createExhibitEditor_withTemplateId_isCorrect() {
        assertEquals("create_exhibit_editor/tpl_small", Screen.CreateExhibitEditor.createRoute("tpl_small"))
        assertEquals("create_exhibit_editor/ex1", Screen.CreateExhibitEditor.createRoute("ex1"))
    }

    @Test
    fun screenRoutes_postsFeed_withPostId_isCorrect() {
        assertEquals("posts_feed?postId=p1", Screen.PostsFeed.createRoute("p1"))
    }
}
