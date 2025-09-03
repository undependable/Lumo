package no.uio.ifi.in2000.team33.lumo.ui.utility.data

class Users(fnavn: String, enavn: String) {
    private var fname: String = fnavn
    private var lname: String = enavn

    init {
        println("$fname $lname")
    }
}