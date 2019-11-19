package com.samco.trackandgraph.displaytrackgroup

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import org.threeten.bp.OffsetDateTime
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.samco.trackandgraph.database.*
import com.samco.trackandgraph.databinding.DataPointInputDialogBinding
import kotlinx.android.synthetic.main.data_point_input_dialog.*
import kotlinx.coroutines.*

const val FEATURE_LIST_KEY = "FEATURE_LIST_KEY"
const val DATA_POINT_TIMESTAMP_KEY = "DATA_POINT_ID"

class InputDataPointDialog : DialogFragment(), ViewPager.OnPageChangeListener {
    private lateinit var viewModel: InputDataPointDialogViewModel
    private val inputViews = mutableMapOf<Int, DataPointInputView>()
    private lateinit var binding: DataPointInputDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return activity?.let {
            viewModel = ViewModelProviders.of(this).get(InputDataPointDialogViewModel::class.java)
            initViewModel()
            binding = DataPointInputDialogBinding.inflate(inflater, container, false)

            binding.cancelButton.setOnClickListener { dismiss() }
            binding.skipButton.setOnClickListener { skip() }
            binding.addButton.setOnClickListener { onAddClicked() }

            listenToFeatures()
            listenToIndex()
            listenToState()

            binding.viewPager.addOnPageChangeListener(this)
            dialog?.setCanceledOnTouchOutside(true)

            binding.root
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun listenToState() {
        viewModel.state.observe(this, Observer { state ->
            when (state) {
                InputDataPointDialogState.LOADING -> { binding.addButton.isEnabled = false }
                InputDataPointDialogState.WAITING -> { binding.addButton.isEnabled = true }
                InputDataPointDialogState.ADDING -> { binding.addButton.isEnabled = false }
                InputDataPointDialogState.ADDED -> {
                    binding.addButton.isEnabled = true
                    if (viewModel.currentFeatureIndex.value!! < viewModel.features.value!!.size-1) {
                        skip()
                        viewModel.onFinishedTransition()
                    }
                    else dismiss()
                }
            }
        })
    }

    private fun listenToIndex() {
        viewModel.currentFeatureIndex.observe(this, Observer { index ->
            if (index != null) setupViewFeature(viewModel.features.value!![index], index)
        })
    }

    private fun listenToFeatures() {
        viewModel.features.observe(this, Observer { features ->
            if (features.isEmpty()) return@Observer
            binding.viewPager.adapter = ViewPagerAdapter(
                context!!,
                features,
                DataPointInputView.DataPointInputClickListener(this::onAddClicked),
                viewModel.uiStates,
                inputViews
            )
            if (features.size == 1) indexText.visibility = View.INVISIBLE
        })
    }

    private fun initViewModel() {
        val timestampStr = arguments!!.getString(DATA_POINT_TIMESTAMP_KEY)
        val timestamp = if (timestampStr != null) odtFromString(timestampStr) else null
        viewModel.loadData(activity!!, arguments!!.getLongArray(FEATURE_LIST_KEY)!!.toList(), timestamp)
    }

    private class ViewPagerAdapter(val context: Context,
                                   val features: List<Feature>,
                                   val clickListener: DataPointInputView.DataPointInputClickListener,
                                   val uiStates: Map<Feature, DataPointInputView.DataPointInputData>,
                                   val inputViews: MutableMap<Int, DataPointInputView>) : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = DataPointInputView(context, uiStates[features[position]]!!)
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.layoutParams = params
            view.setOnClickListener(clickListener)
            inputViews[position] = view
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount() = features.size
    }

    override fun onPageScrollStateChanged(state: Int) { }
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
    override fun onPageSelected(position: Int) { viewModel.currentFeatureIndex.value = position }

    private fun setupViewFeature(feature: Feature, index: Int) {
        if (feature.featureType == FeatureType.DISCRETE) binding.addButton.visibility = View.GONE
        else binding.addButton.visibility = View.VISIBLE
        if (index == viewModel.features.value!!.size-1) binding.skipButton.visibility = View.GONE
        else binding.skipButton.visibility = View.VISIBLE
        indexText.text = "${index+1} / ${viewModel.features.value!!.size}"

        //SHOW/HIDE KEYBOARD
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (feature.featureType == FeatureType.CONTINUOUS) imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        else imm.hideSoftInputFromWindow(view?.windowToken, 0)
        activity!!.currentFocus?.clearFocus()
    }


    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun onAddClicked() {
        val currIndex = viewModel.currentFeatureIndex.value!!
        val currFeature = viewModel.features.value!![currIndex]
        onAddClicked(currFeature)
    }

    private fun onAddClicked(feature: Feature) {
        onSubmitResult(viewModel.uiStates[feature]!!)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity!!.currentFocus?.clearFocus()
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity!!.window.decorView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun onSubmitResult(dataPointInputData: DataPointInputView.DataPointInputData) {
        viewModel.onDataPointInput(activity!!,
            DataPoint(dataPointInputData.dateTime, dataPointInputData.feature.id,
                dataPointInputData.value, dataPointInputData.label)
        )
    }

    private fun skip() = binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1, true)
}

enum class InputDataPointDialogState { LOADING, WAITING, ADDING, ADDED }
class InputDataPointDialogViewModel : ViewModel() {
    private var updateJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + updateJob)

    val state: LiveData<InputDataPointDialogState> get() { return _state }
    private val _state by lazy {
        val state = MutableLiveData<InputDataPointDialogState>()
        state.value = InputDataPointDialogState.LOADING
        return@lazy state
    }

    lateinit var uiStates: Map<Feature, DataPointInputView.DataPointInputData>
    val features: LiveData<List<Feature>> get() { return _features }
    private val _features by lazy {
        val feats = MutableLiveData<List<Feature>>()
        feats.value = listOf()
        return@lazy feats
    }

    val currentFeatureIndex: MutableLiveData<Int?> get() { return _currentFeatureIndex }
    private val _currentFeatureIndex by lazy {
        val index = MutableLiveData<Int?>()
        index.value = null
        return@lazy index
    }

    fun loadData(activity: Activity, featureIds: List<Long>, dataPointTimestamp: OffsetDateTime?) {
        if (features.value!!.isNotEmpty()) return
        val application = activity.application
        val dao = TrackAndGraphDatabase.getInstance(application).trackAndGraphDatabaseDao
        uiScope.launch {
            _state.value = InputDataPointDialogState.LOADING
            lateinit var featureData: List<Feature>
            var dataPointData: DataPoint? = null
            withContext(Dispatchers.IO) {
                featureData = dao.getFeaturesByIdsSync(featureIds)
                if (dataPointTimestamp != null) {
                    dataPointData = dao.getDataPointByTimestampAndFeatureSync(featureData[0].id, dataPointTimestamp)
                }
            }

            val defaultValue = if (dataPointData != null) dataPointData!!.value else 0.toDouble()
            val defaultLabel = if (dataPointData != null) dataPointData!!.label else ""
            val timestamp = if (dataPointData != null) dataPointData!!.timestamp else OffsetDateTime.now()
            val timeModifiable = dataPointData == null
            uiStates = featureData.map { f ->
                f to DataPointInputView.DataPointInputData(f, timestamp, defaultValue, defaultLabel, timeModifiable)
            }.toMap()
            _features.value = featureData
            _currentFeatureIndex.value = 0
            _state.value = InputDataPointDialogState.WAITING
        }
    }

    fun onDataPointInput(activity: Activity, dataPoint: DataPoint) {
        if (state.value != InputDataPointDialogState.WAITING) return
        val application = activity.application
        val dao = TrackAndGraphDatabase.getInstance(application).trackAndGraphDatabaseDao
        uiScope.launch {
            _state.value = InputDataPointDialogState.ADDING
            withContext(Dispatchers.IO) {
                dao.insertDataPoint(dataPoint)
            }
            _state.value = InputDataPointDialogState.ADDED
        }
    }

    fun onFinishedTransition() {
        _state.value = InputDataPointDialogState.WAITING
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}
