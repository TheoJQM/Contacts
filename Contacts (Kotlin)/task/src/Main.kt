package contacts

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

abstract class Record(var name: String) {
    lateinit var timeCreated: String
    lateinit var timeLastEdit: String

    var phoneNumber: String = ""
        get() = if (field == "") "[no number]" else field
        set(value) {
            val phoneRegex = Regex("""\+?(\(?\p{Alnum}+\)?|\p{Alnum}+[ -]\(?\p{Alnum}{2,}\)?)([ -]\p{Alnum}{2,})*""")
            field = if (phoneRegex.matches(value.trim()))  value.trim() else { println("Wrong number format!"); "" }
        }

    abstract fun editRecord()
    abstract fun getAllValue(): String

    open fun showName() = "$name "

    override fun toString(): String {
        return """
            
            Number: $phoneNumber
            Time created: $timeCreated
            Time last edit: $timeLastEdit
        """.trimIndent()
    }
}

class Person(name: String, private var surname: String) : Record(name) {
    var birthDate: String = ""
        get() = if (field == "") "[no data]" else field
        set(value) {
            val dateRegex = Regex("""\d{4}-\d{2}-\d{2}""")
            field = if (dateRegex.matches(value.trim())) value.trim() else { println("Bad birth date!"); "" }
        }

    var gender: String = ""
        get() = if (field == "") "[no data]" else field
        set(value) {
            val genderRegex = Regex("""[MF]""")
            field = if (genderRegex.matches(value.trim())) value.trim() else { println("Bad gender!"); "" }
        }

    override fun editRecord() {
        val field = print("Select a field (name, surname, birth, gender, number): ").run { readln() }

        val newValue = print("Enter $field: ").run { readln() }
        when (field) {
            "name" -> this.name = newValue
            "surname" -> this.surname = newValue
            "birth" -> this.birthDate = newValue
            "gender" -> this.gender = newValue
            "number" -> this.phoneNumber = newValue
            else -> println("Error field\n")
        }

        if (field in arrayOf("name", "surname", "birth", "gender", "number")) {
            this.timeLastEdit = formatter.format(LocalDateTime.now())
            println("Saved")
            println(this.toString() + "\n")
        }
    }

    override fun getAllValue() = "$name $surname $birthDate $gender $phoneNumber $timeCreated $timeLastEdit".lowercase()

    override fun showName() = super.showName() + surname

    override fun toString(): String {
        return """
            Name: $name
            Surname: $surname
            Birth date: $birthDate
            Gender: $gender
        """.trimIndent() + super.toString()
    }
}

class Organisation(name: String, private var address: String) : Record(name) {

    override fun editRecord() {
        val field = print("Select a field (address, number): ").run { readln() }

        val newValue = print("Enter $field: ").run { readln() }
        when (field) {
            "address" -> this.address = newValue
            "number" -> this.phoneNumber = newValue
            else -> println("Error field\n")
        }

        if (field in arrayOf("address", "number")) {
            this.timeLastEdit = formatter.format(LocalDateTime.now())
            println("Saved")
            println(this.toString() + "\n")
        }
    }

    override fun getAllValue() = "$name $phoneNumber $timeCreated $timeLastEdit".lowercase()

    override fun toString(): String {
        return """
            Organization name: $name
            Address: $address
        """.trimIndent() + super.toString()
    }
}

class Contact {
    private val records = mutableListOf<Record>()
    private var exit = true

    fun useContact() {
        while (exit) {
            val action = print("[menu] Enter action (add, list, search, count, exit): ").run { readln() }
            when (action) {
                "add" -> addRecord()
                "list" -> showRecords()
                "search" -> search()
                "count" -> countRecords()
                "exit" -> exit = false
            }
        }
    }



    private fun addRecord() {
        val type = print("Enter the type (person, organization): ").run { readln() }
        when (type) {
            "person" -> addPerson()
            "organization" -> addOrganization()
            else -> println("Error Type\n")
        }
    }

    private fun addPerson() {
        // Get Name and Surname
        val name = print("Enter the name: ").run { readln() }
        val surname = print("Enter the surname: ").run { readln() }
        val person = Person(name, surname)

        // Get Birthdate
        person.birthDate = print("Enter the birth date: ").run { readln() }

        // Get Gender
        person.gender = print("Enter the gender (M, F): ").run { readln() }

        // Get Phone number
        person.phoneNumber = print("Enter the number: ").run { readln() }

        // Initialise the time of creation and lastEdit
        person.timeCreated = formatter.format(LocalDateTime.now())
        person.timeLastEdit = formatter.format(LocalDateTime.now())

        records.add(person)
        println("The record added.\n")
    }

    private fun addOrganization() {
        // Get Name and Address
        val name = print("Enter the organization name: ").run { readln() }
        val address = print("Enter the address: ").run { readln() }
        val organisation = Organisation(name, address)

        // Get Phone number
        organisation.phoneNumber = print("Enter the number: ").run { readln() }

        // Initialise the time of creation and lastEdit
        organisation.timeCreated = formatter.format(LocalDateTime.now())
        organisation.timeLastEdit = formatter.format(LocalDateTime.now())
        records.add(organisation)
        println("The record added.\n")
    }



    private fun showRecords() {
        for (i in records.indices) println("${i + 1}. ${records[i].showName()}")
        val index = print("[list] Enter action ([number], back): ").run { readln() }
        if (index == "back") return else updateRecord(index.toInt() - 1)
    }

    private fun search() {
        var again = true
        while (again) {
            val query = print("Enter search query: ").run { readln().lowercase() }
            val resultIndices = records.filter { it.getAllValue().contains(query.trim()) }.indices.toList()

            if (resultIndices.isEmpty()) {
                println("No result\n")
            } else {
                println("Found ${resultIndices.size} results:")
                for (i in resultIndices.indices) println("${i + 1}. ${records[i].showName()}")
            }

            val action = print("[search] Enter action ([number], back, again): ").run { readln() }
            when (action) {
                "back" -> again = false
                "again" -> continue
                else -> updateRecord(action.toInt() - 1)
            }
        }
    }

    private fun updateRecord(index: Int) {
        var record = true

        println(records[index].toString() + "\n")

        while (record) {
            val action = print("[record] Enter action (edit, delete, menu): ").run { readln() }
            when (action) {
                "edit" -> records[index].editRecord()
                "delete" -> deleteRecord(index)
                "menu" -> record = false
            }
        }
    }

    private fun deleteRecord(index: Int) {
        records.removeAt(index)
        println("Record removed!\n")
    }



    private fun countRecords() = println("The Phone Book has ${records.size} records.\n")
}

fun main() {
    val contacts = Contact()
    contacts.useContact()
}