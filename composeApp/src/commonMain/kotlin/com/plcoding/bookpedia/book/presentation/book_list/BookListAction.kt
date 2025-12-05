package com.plcoding.bookpedia.book.presentation.book_list

import com.plcoding.bookpedia.book.domain.Book

sealed interface BookListAction {
    data class onSearchQueryChange(val query: String) : BookListAction
    data class onBookClick(val book: Book) : BookListAction
    data class onTabSelected(val index: Int) : BookListAction
}