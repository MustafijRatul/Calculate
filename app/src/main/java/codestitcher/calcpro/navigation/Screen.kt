package codestitcher.calcpro.navigation

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator")
    object CurrencyConverter : Screen("currency_converter")
    object UnitConverter : Screen("unit_converter")
}
