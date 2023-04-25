rootProject.name = "kkhouse.com.englishconversationserver"
include("infrastructure:repository")
include("infrastructure:network")
include("domain:model")
include("domain:repository")
include("application")
include("domain:adapters")
include("infrastructure:database")
findProject(":infrastructure:database")?.name = "database"
