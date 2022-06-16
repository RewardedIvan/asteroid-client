const axios = require("axios").default
const FormData = require("form-data")
const fs = require("fs")

const branch = process.argv[2]
const version = process.argv[3]
const build = process.argv[4]
const compareUrl = process.argv[5]


// Discord webhook
axios
    .get(compareUrl)
    .then(async (res) => {
        let success = true
        let description = ""
        const Artifacts = JSON.parse(await axios.get("https://circleci.com/api/v2/project/github/RewardedIvan/asteroid-client/" + build + "/artifacts"))

        description += "**Branch:** " + branch
        description += "\n**Status:** " + (success ? "success" : "failure")

        let changes = "\n\n**Changes:**"
        let hasChanges = false
        for (let i in res.data.commits) {
            let commit = res.data.commits[i]

            changes += "\n- [`" + commit.sha.substring(0, 7) + "`](https://github.com/RewardedIvan/asteroid-client/commit/" + commit.sha + ") *" + commit.commit.message + "*"
            hasChanges = true
        }
        if (hasChanges) description += changes

        if (success) {
            description += "\n\n**Download:** [asteroid-client-" + version + "-" + build + "](" + Artifacts["items"][3] + ")"
        }
        
        const webhook = {
            username: "Asteroid Client - CircleCI Builds",
            avatar_url: "https://raw.githubusercontent.com/RewardedIvan/asteroid-client/main/src/main/resources/assets/asteroid-client/textures/asteroid.png",
            embeds: [
                {
                    title: "asteroid client v" + version + " build #" + build,
                    description: description,
                    url: "https://github.com/RewardedIvan/asteroid-client",
                    color: success ? 3066993 : 15158332
                }
            ]
        };

        axios.post(process.env.discord_webhook, webhook);
    })
