package com.kronosempire.casos.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kronosempire.casos.data.model.Registro
import com.kronosempire.casos.data.repository.RegistroRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: RegistroRepository) : ViewModel() {
    private val _registros = MutableLiveData<List<Registro>>()
    val registros: LiveData<List<Registro>> = _registros

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun cargarRegistrosPorFecha(fecha: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRegistrosPorFecha(fecha).collect { lista ->
                _registros.value = lista
                _isLoading.value = false
            }
        }
    }

    fun cargarRegistrosPorPeriodo(inicio: String, fin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getRegistrosPorPeriodo(inicio, fin).collect { lista ->
                _registros.value = lista
                _isLoading.value = false
            }
        }
    }

    fun buscarGlobal(query: String) {
        if (query.length < 2) {
            _registros.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.buscarGlobal(query).collect { lista ->
                _registros.value = lista
                _isLoading.value = false
            }
        }
    }
}
