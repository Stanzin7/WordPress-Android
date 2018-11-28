package org.wordpress.android.ui.stats.refresh.lists.sections.insights

import android.arch.lifecycle.LiveData
import kotlinx.coroutines.experimental.CoroutineDispatcher
import org.wordpress.android.modules.UI_THREAD
import org.wordpress.android.ui.stats.refresh.INSIGHTS_USE_CASE
import org.wordpress.android.ui.stats.refresh.lists.BaseListUseCase
import org.wordpress.android.ui.stats.refresh.lists.NavigationTarget
import org.wordpress.android.ui.stats.refresh.lists.StatsListViewModel
import org.wordpress.android.ui.stats.refresh.lists.StatsUiState
import org.wordpress.android.ui.stats.refresh.lists.StatsUiState.StatsListState.DONE
import org.wordpress.android.util.map
import javax.inject.Inject
import javax.inject.Named

class InsightsListViewModel
@Inject constructor(
    @Named(UI_THREAD) mainDispatcher: CoroutineDispatcher,
    @Named(INSIGHTS_USE_CASE) private val insightsUseCase: BaseListUseCase
) : StatsListViewModel(mainDispatcher) {
    private val _data = insightsUseCase.data.map { loadedData ->
        StatsUiState(loadedData, DONE)
    }
    override val data: LiveData<StatsUiState> = _data

    override val navigationTarget: LiveData<NavigationTarget> = insightsUseCase.navigationTarget

    override fun onCleared() {
        insightsUseCase.onCleared()
        super.onCleared()
    }
}
