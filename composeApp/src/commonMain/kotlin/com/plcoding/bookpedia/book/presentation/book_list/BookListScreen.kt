package com.plcoding.bookpedia.book.presentation.book_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmp_bookpedia.composeapp.generated.resources.Res
import cmp_bookpedia.composeapp.generated.resources.favourites
import cmp_bookpedia.composeapp.generated.resources.no_favourites_books
import cmp_bookpedia.composeapp.generated.resources.no_search_results
import cmp_bookpedia.composeapp.generated.resources.search_results
import com.plcoding.bookpedia.book.domain.Book
import com.plcoding.bookpedia.book.presentation.book_list.components.BookList
import com.plcoding.bookpedia.book.presentation.book_list.components.BookSearchBar
import com.plcoding.bookpedia.core.presentation.DarkBlue
import com.plcoding.bookpedia.core.presentation.DesertWhite
import com.plcoding.bookpedia.core.presentation.SandYellow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookListScreenRoot(
    viewModel: BookListViewModel,
    onBookClick: (Book) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BookListScreen(
        state = state, onAction = { action ->
            when (action) {
                is BookListAction.onBookClick -> {
                    onBookClick(action.book)
                }

                else -> Unit
            }
            viewModel.onAction(action)
        })
}


@Composable
private fun BookListScreen(
    state: BookListState, onAction: (BookListAction) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val pagerState = rememberPagerState { 2 }
    val searchListLazyState = rememberLazyListState()
    val favouriteListLazyState = rememberLazyListState()


    LaunchedEffect(state.searchResults) {
        searchListLazyState.animateScrollToItem(0)
    }

    LaunchedEffect(state.selectedTabIndex) {
        pagerState.animateScrollToPage(state.selectedTabIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        onAction(BookListAction.onTabSelected(pagerState.currentPage))
    }

    Column(
        modifier = Modifier.background(DarkBlue).fillMaxSize().statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BookSearchBar(
            searchQuery = state.searchQuery, onSearchQueryChange = { query ->
                onAction(BookListAction.onSearchQueryChange(query))
            }, onImeSearch = {
                keyboardController?.hide()
            }, modifier = Modifier.widthIn(400.dp).fillMaxWidth().padding(16.dp)
        )
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(), color = DesertWhite, shape = RoundedCornerShape(
                topStart = 32.dp, topEnd = 32.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    modifier = Modifier.padding(vertical = 12.dp).widthIn(max = 700.dp).fillMaxWidth(),
                    selectedTabIndex = state.selectedTabIndex,
                    containerColor = DesertWhite,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            color = SandYellow,
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTabIndex])
                        )

                    }
                ) {
                    Tab(
                        selected = state.selectedTabIndex == 0,
                        onClick = {
                            onAction(BookListAction.onTabSelected(0))
                        },
                        modifier = Modifier.weight(1f),
                        selectedContentColor = SandYellow,
                        unselectedContentColor = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = stringResource(Res.string.search_results),
                            modifier = Modifier
                                .padding(vertical = 12.dp)


                        )
                    }
                    Tab(
                        selected = state.selectedTabIndex == 1,
                        onClick = {
                            onAction(BookListAction.onTabSelected(1))
                        },
                        modifier = Modifier.weight(1f),
                        selectedContentColor = SandYellow,
                        unselectedContentColor = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = stringResource(Res.string.favourites),
                            modifier = Modifier
                                .padding(vertical = 12.dp)


                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { pageIndex ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    )
                    {
                        when (pageIndex) {
                            0 -> {
                                if (state.isLoading) {
                                    CircularProgressIndicator()
                                } else {
                                    when {
                                        state.errorMessage != null -> {
                                            Text(
                                                text = state.errorMessage.asString(),
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }

                                        state.searchResults.isEmpty() -> {
                                            Text(
                                                text = stringResource(Res.string.no_search_results),
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.headlineSmall,
                                            )
                                        }

                                        else -> {
                                            BookList(
                                                books = state.searchResults,
                                                onBookClick = {
                                                    onAction(BookListAction.onBookClick(it))
                                                },
                                                modifier = Modifier.fillMaxSize(),
                                                scrollState = searchListLazyState,
                                            )
                                        }
                                    }
                                }
                            }

                            1 -> {
                                if (state.favouriteBooks.isEmpty()) {
                                    Text(
                                        text = stringResource(Res.string.no_favourites_books),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                } else {
                                    BookList(
                                        books = state.favouriteBooks,
                                        onBookClick = {
                                            onAction(BookListAction.onBookClick(it))
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                        scrollState = favouriteListLazyState,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewBookListScreen() {
    BookListScreen(
        state = BookListState(
            searchResults = sampleBooks
        ), onAction = {})
}

val sampleBooks = (1..20).map { id ->
    Book(
        id = id.toString(),
        title = "Sample Book $id",
        imageUrl = "https://example.com/book_$id.jpg",
        authors = listOf("Author $id"),
        description = "This is a description for Sample Book $id.",
        languages = listOf("English"),
        firstPublishYear = "20${10 + (id % 15)}",
        averageRating = (3..5).random() + listOf(0.0, 0.1, 0.2, 0.5).random(),
        ratingCount = (1000..50000).random(),
        numPages = (150..600).random(),
        numEditions = (1..50).random()
    )
}
