package com.example.redminecapstoneproject.ui.loginsignup

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.redminecapstoneproject.LoadingUtils
import com.example.redminecapstoneproject.R
import com.example.redminecapstoneproject.RepoViewModelFactory
import com.example.redminecapstoneproject.databinding.FragmentLoginBinding
import com.example.redminecapstoneproject.ui.home.HomeActivity
import com.example.redminecapstoneproject.ui.donordata.DonorDataActivity
import com.example.redminecapstoneproject.ui.otp.OtpActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import www.sanju.motiontoast.MotionToast

class LoginFragment : Fragment(), View.OnFocusChangeListener {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var isEmailValid: Boolean = false
    private var isPassValid: Boolean = false
    private lateinit var googleSignInClient: GoogleSignInClient
    private var counter2 = 0
    private var startIntent = true

    private val resultCOntract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            try {
                val loginSignupViewModel = ViewModelProvider(
                    requireActivity(),
                    RepoViewModelFactory.getInstance(requireActivity())
                )[LoginSignupViewModel::class.java]

                val task = GoogleSignIn.getSignedInAccountFromIntent(result?.data)

                val exception = task.exception
                if (task.isSuccessful) {
                    try {
                        val account = task.getResult(ApiException::class.java)
                        Log.d("TAG", "firebaseAuthWithGoogle:" + account.id)
                        loginSignupViewModel.firebaseAuthWithGoogle(account.idToken.toString())
                    } catch (e: ApiException) {
                        Log.w("TAG", "Google sign in failed", e)
                    }
                } else {
                    Log.w("TAG", "error " + exception?.message.toString())
                }
            } catch (e: Exception) {
                Log.e("ERROR", e.message.toString())
            }


        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etEmail.onFocusChangeListener = this
        binding.etPassword.onFocusChangeListener = this

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id_2))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val loginSignupViewModel = ViewModelProvider(
            requireActivity(),
            RepoViewModelFactory.getInstance(requireActivity())
        )[LoginSignupViewModel::class.java]


        binding.btGoogle.setOnClickListener {
            signIn()
        }

        binding.cbSeePassword.setOnClickListener {
            if (binding.cbSeePassword.isChecked) {
                binding.etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        binding.btSignUp.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
            activity?.supportFragmentManager?.popBackStack()

        }

        binding.btLogin.setOnClickListener {

            if (isDataValid()) {
                val email = binding.etEmail.text.toString().trim()
                val pass = binding.etPassword.text.toString().trim()

                loginSignupViewModel.login(email, pass)

            } else {
                if (!isFieldsEmpty()) {
                    MotionToast.Companion.createColorToast(
                        requireActivity(),
                        getString(R.string.hey_careful),
                        getString(R.string.please_enter_data_correctly),
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(
                            requireActivity(),
                            www.sanju.motiontoast.R.font.helvetica_regular
                        )
                    )
                }
            }

        }
        loginSignupViewModel.message.observe(requireActivity()) {
            if (isAdded) makeToast(it.first, it.second)
        }

        //val intent = Intent(activity, LoginActivity::class.java)
        val intent2 = Intent(activity, DonorDataActivity::class.java)
        val intent3 = Intent(activity, HomeActivity::class.java)
        val intent4 = Intent(activity, OtpActivity::class.java)

        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)


        if (startIntent) {
            loginSignupViewModel.firebaseUser.observe(requireActivity()) { fu ->
                if (fu == null) {
                    //startActivity(intent)
                } else {
                    if (activity != null) {
                        loginSignupViewModel.getUserAccountDataDb()
                            .observe(requireActivity()) { value ->
                                //Log.d("TES",value.toString())
                                if (value == null) {
                                    counter2++
                                    if (counter2 > 1) {
                                    loginSignupViewModel.setUserAccountData()
                                    }
                                } else if (value.otpCode == null) {
                                    intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startIntent = false
                                    startActivity(intent4)
                                } else {
                                    loginSignupViewModel.getUserDonorDataDb()
                                        .observe(requireActivity()) {
                                            if (it == null) {
                                                /*counter++
                                                if (counter > 1) {
                                                    counter = 0*/
                                                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                intent2.putExtra("name", value.name)
                                                startIntent = false
                                                startActivity(intent2)
                                                //}
                                            } else {
                                                //intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                startIntent = false
                                                startActivity(intent3)
                                                if (isAdded) {
                                                    activity?.finish()
                                                }
                                            }
                                        }
                                }
                            }
                    }


                }
            }
        }

        loginSignupViewModel.isLoading.observe(requireActivity()) {
            showLoading(it)
        }


    }

    private fun showLoading(show: Boolean) {
        if (show) {
            if (isAdded) {
                LoadingUtils.showDialog(context, false)
            }
        } else {
            LoadingUtils.hideDialog()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultCOntract.launch(signInIntent)
    }


    private fun makeToast(isError: Boolean, msg: String) {
        if (isError) {
            MotionToast.Companion.createColorToast(
                requireActivity(),
                "Ups",
                msg,
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(
                    requireActivity(),
                    www.sanju.motiontoast.R.font.helvetica_regular
                )
            )
        } else {
            MotionToast.Companion.createColorToast(
                requireActivity(),
                getString(R.string.yey_success),
                msg,
                MotionToast.TOAST_SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(
                    requireActivity(),
                    www.sanju.motiontoast.R.font.helvetica_regular
                )
            )
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun isFieldsEmpty(): Boolean {
        return binding.etEmail.text.toString().trim() == ""
                && binding.etPassword.text.toString().trim() == ""

    }

    private fun isDataValid(): Boolean {
        binding.etEmail.clearFocus()
        binding.etPassword.clearFocus()
        return isEmailValid && isPassValid
    }

    private fun checkEmail() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            isEmailValid = false
            binding.ilEmail.error = getString(R.string.input_email)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isEmailValid = false
            binding.ilEmail.error = getString(R.string.invalid_email)
        } else {
            isEmailValid = true
        }
    }


    private fun checkPass() {
        val pass = binding.etPassword.text.toString().trim()
        if (pass.isEmpty()) {
            isPassValid = false
            binding.ilPassword.error = getString(R.string.input_pass)
        } else if (pass.length < 6) {
            isPassValid = false
            binding.ilPassword.error = getString(R.string.invalid_password)
        } else {
            isPassValid = true
        }
    }

    override fun onFocusChange(v: View?, isFocused: Boolean) {
        if (v != null) {
            when (v.id) {
                R.id.et_email -> {
                    if (isFocused) {
                        binding.ilEmail.isErrorEnabled = false
                        binding.ilEmail.error = ""
                    } else {
                        checkEmail()
                    }
                }
                R.id.et_password -> {
                    if (isFocused) {
                        binding.ilPassword.isErrorEnabled = false
                        binding.ilPassword.error = ""
                    } else {
                        checkPass()
                    }
                }
            }
        }
    }

}