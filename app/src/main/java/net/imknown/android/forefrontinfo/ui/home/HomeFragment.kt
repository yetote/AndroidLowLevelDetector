package net.imknown.android.forefrontinfo.ui.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import net.imknown.android.forefrontinfo.MyApplication
import net.imknown.android.forefrontinfo.base.BaseListFragment

class HomeFragment : BaseListFragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    override val listViewModel by activityViewModels<HomeViewModel>()

    override fun init() {
        listViewModel.subtitle.observe(viewLifecycleOwner, Observer {
            val actionBar = (activity as AppCompatActivity).supportActionBar
            actionBar?.subtitle = MyApplication.getMyString(it.lldDataModeResId, it.dataVersion)
        })

        listViewModel.models.observe(viewLifecycleOwner, Observer {
            listViewModel.showModels(myAdapter, it)
        })

        listViewModel.error.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { exception ->
                listViewModel.showError(exception)
            }
        })
    }
}
