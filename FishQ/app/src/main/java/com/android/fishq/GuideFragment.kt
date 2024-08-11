package com.android.fishq

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// Mendefinisikan kelas GuideFragment
class GuideFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Layout dengan tampilan dari "fragment_guide.xml"
        return inflater.inflate(R.layout.fragment_guide, container, false)
    }
}