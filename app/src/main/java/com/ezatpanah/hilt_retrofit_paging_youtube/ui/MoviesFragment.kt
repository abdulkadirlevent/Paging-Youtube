package com.ezatpanah.hilt_retrofit_paging_youtube.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezatpanah.hilt_retrofit_paging_youtube.R
import com.ezatpanah.hilt_retrofit_paging_youtube.adapter.LoadMoreAdapter
import com.ezatpanah.hilt_retrofit_paging_youtube.adapter.MoviesAdapter
import com.ezatpanah.hilt_retrofit_paging_youtube.databinding.FragmentMoviesBinding
import com.ezatpanah.hilt_retrofit_paging_youtube.viewmodel.MoviesViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MoviesFragment : Fragment() {

    private lateinit var binding: FragmentMoviesBinding

    @Inject
    lateinit var moviesAdapter: MoviesAdapter

    private val viewModel: MoviesViewModel by viewModels()

     fun onCreateView2323(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMoviesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMoviesBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        // Toolbar'ı bul
        val toolbar: Toolbar = view.findViewById(R.id.toolbarMovies)

        // Bu Fragment'ın kendi ActionBar'ını ayarla
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
       // (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_round_star_24)

        toolbar.setNavigationOnClickListener {
            val drawerLayout: DrawerLayout = view.findViewById(R.id.drawerLayout)
            drawerLayout.openDrawer(GravityCompat.START)
        }
        // Geri tuşunu göster
        // (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // ActionBar'ın başlığını ayarla
        (activity as AppCompatActivity).supportActionBar?.title = "Movies"

        // Diğer onCreateView işlemlerini buraya ekle

        // DrawerLayout ve NavigationView'ı bul
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = view.findViewById(R.id.navigationView)

        // ActionBarDrawerToggle ekleyin
        val toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //R.id.nav_projects -> replaceFragment(ProjectsFragment())

                R.id.nav_projects -> {
                    // ProjectsFragment'ı aç
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.navHost, ProjectsFragment())
                        .commit()
                }
                // R.id.nav_installations -> replaceFragment(InstallationsFragment())
                // R.id.nav_users -> replaceFragment(UsersFragment())
                // R.id.nav_accounts -> replaceFragment(AccountsFragment())
                // R.id.nav_notifications -> replaceFragment(NotificationsFragment())
                // R.id.nav_overtimes -> replaceFragment(OvertimesFragment())
                // R.id.nav_permits -> replaceFragment(PermitsFragment())
                // R.id.nav_expenses -> replaceFragment(ExpensesFragment())
            }
            // Seçilen öğeyi vurgula
            menuItem.isChecked = true
            // Drawer'ı kapat
            val drawerLayout: DrawerLayout = view.findViewById(R.id.drawerLayout)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        return view
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.moviesList.collect {
                       moviesAdapter.submitData(it)
                    }
                }
            }
            /*
            lifecycleScope.launchWhenCreated {
                viewModel.moviesList.collect {
                    moviesAdapter.submitData(it)
                }
            }
            */
            moviesAdapter.setOnItemClickListener {
                val direction = MoviesFragmentDirections.actionMoviesFragmentToMovieDetailsFragment(it.id)
                findNavController().navigate(direction)
            }

            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    moviesAdapter.loadStateFlow.collect {
                        val state = it.refresh
                        prgBarMovies.isVisible = state is LoadState.Loading
                    }
                }
            }
            /*
            lifecycleScope.launchWhenCreated {
                moviesAdapter.loadStateFlow.collect {
                    val state = it.refresh
                    prgBarMovies.isVisible = state is LoadState.Loading
                }
            }
            */
            rlMovies.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = moviesAdapter
            }

            rlMovies.adapter = moviesAdapter.withLoadStateFooter(
                LoadMoreAdapter {
                    moviesAdapter.retry()
                }
            )

        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.navHost, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}