package com.my.mapupassessment.ui.history

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.my.mapupassessment.R
import com.my.mapupassessment.adapters.SessionHistoryAdapter
import com.my.mapupassessment.databinding.FragmentSessionHistoryBinding
import com.my.mapupassessment.utils.Helper.formatDistance
import com.my.mapupassessment.utils.gone
import com.my.mapupassessment.utils.showSnack
import com.my.mapupassessment.utils.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SessionHistoryFragment : Fragment() {

    companion object{
        private const val TAG = "Session History Fragment"
    }

    private lateinit var binding: FragmentSessionHistoryBinding

    private val viewModel: SessionHistoryViewModel by viewModels()
    private lateinit var adapter: SessionHistoryAdapter

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSessionHistoryBinding.inflate(inflater, container, false)


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SessionHistoryAdapter { session ->
            if(session.distance > 50){
                // Navigate to detail fragment
                findNavController().navigate(
                    R.id.action_history_to_detail,
                    bundleOf("sessionId" to session.id)
                )
            }else{
                view.showSnack("No route create for that session.")
            }

        }
        binding.rvSessions.adapter = adapter
        binding.rvSessions.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allSessions.observe(viewLifecycleOwner) {list->
            if(list.isEmpty()){
                binding.emptyListLayout.visible()
            }else{
                binding.emptyListLayout.gone()
            }
            adapter.submitList(list)
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

    }

}