package fi.hsl.jore4.auth

/**
 * Define the Spring profiles that can be used in different environments.
 */
class Profiles {

    companion object {

        const val APPLICATION = "!integrationTest"
        const val INTEGRATION_TEST = "integrationTest"
    }
}