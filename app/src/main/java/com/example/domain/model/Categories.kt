package com.example.domain.model

object Categories {
    val incomeCategories = listOf(
        "Salaire job étudiant",
        "Bourse",
        "Aide familiale",
        "Remboursement prêt",
        "Autre entrée"
    )

    val expenseCategories = listOf(
        "Logement",
        "Courses",
        "Transport",
        "Études",
        "Projet étude",
        "Santé",
        "Loisirs",
        "Remboursement de prêt",
        "Autre dépense"
    )

    val savingCategories = listOf(
        "Urgence",
        "Projet étude",
        "Ordinateur",
        "Voyage",
        "Frais universitaires",
        "Autre objectif"
    )

    /**
     * Checks if a category requires custom input
     */
    fun isCustomCategory(category: String): Boolean {
        return category == "Autre entrée" || 
               category == "Autre dépense" || 
               category == "Autre objectif"
    }
}
