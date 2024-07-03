package ai.passio.nutrition.uimodule.ui.dashboard

import ai.passio.nutrition.uimodule.ui.base.BaseViewModel

class DashboardViewModel : BaseViewModel() {

    fun navigateToDiary() {
        navigate(DashboardFragmentDirections.dashboardToSearch())
    }

}