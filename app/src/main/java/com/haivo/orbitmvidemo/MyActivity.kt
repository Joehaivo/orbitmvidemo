package com.haivo.orbitmvidemo

import android.app.ProgressDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.ViewModel
import com.haivo.orbitmvidemo.databinding.ActivityPostBinding
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import org.orbitmvi.orbit.viewmodel.observe

class MyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostBinding
    private val viewModel: MyViewModel by viewModels()
    private val loading by lazy {
        ProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLike.setOnClickListener {
            viewModel.like()
        }

        viewModel.observe(this, state = ::render, sideEffect = ::handleEffect)
    }

    private fun render(state: MyState) {
        binding.tvLike.text = state.likeCount.toString()
        TextViewCompat.setCompoundDrawableTintList(binding.tvLike, ColorStateList.valueOf(if (state.isLike) Color.RED else Color.GRAY))
    }

    private fun handleEffect(effect: MyEffect) {
        when (effect) {
            is MyEffect.Toast -> {
                Toast.makeText(this, effect.text, Toast.LENGTH_SHORT).show()
            }

            is MyEffect.Loading -> {
                if (effect.show) {
                    loading.setTitle(effect.text)
                    loading.show()
                } else {
                    loading.dismiss()
                }
            }
        }
    }
}

data class MyState(
    val isLike: Boolean = false,
    val likeCount: Int = 0,
)

sealed class MyEffect {
    data class Loading(val show: Boolean, val text: String? = null) : MyEffect()
    data class Toast(val text: String) : MyEffect()
}

class MyViewModel : ViewModel(), ContainerHost<MyState, MyEffect> {
    override val container = container<MyState, MyEffect>(MyState(likeCount = 232))

    fun like() = intent {
        if (state.isLike) {
            postSideEffect(MyEffect.Toast("已点赞，不可重复点赞"))
            return@intent
        }
        postSideEffect(MyEffect.Loading(show = true, text = "请稍候..."))
        delay(500) // 模拟网络请求
        reduce {
            // 在此处生成新的state对象, 以便UI观察这个新state而改变
            state.copy(isLike = true, likeCount = state.likeCount + 1)
        }
        postSideEffect(MyEffect.Loading(show = false))
        postSideEffect(MyEffect.Toast("点赞成功"))
    }
}