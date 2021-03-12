# DerpBot

Need a perfectly-timed interjection to lighten up a conversation? Want to mock a friend by repeating their catchphrases?
DerpBot is the perfect tool for the job.

I'm aware that there are quite a few Discord bots named "DerpBot"—SEO wasn't exactly the focus here.

## Directory Structure

Add your `.env` and `data.json` files into the directory tree like this:

* DerpBot
    * src
        * main
            * java
                * DerpBot.java
                * MessageDispatcher.java
            * resources
                * .env
                * data.json

*Note: A number of files aren't listed here for brevity.*

## The `.env` file

Your `.env` file contains basic configuration data for your bot that you don't want in the source code or checked into
Git. At the moment, there are three variables that you can configure through this file: the bot's Discord API token, the
path to your `data.json` (which you shouldn't need to change), and a global control for the likelihood that a trigger
phrase causes the bot to emit a response.

Here's an example:

```
TOKEN=abc123
PATH_TO_JSON=data.json
GLOBAL_AFFINITY=100
```

If you don't have unusual requirements, you can keep the data path and affinity values at the defaults.

## The `data.json` file

The bot's data file follows this pattern:

* Outer list of all triggers
    * First trigger object
        * Trigger phrase
        * Affinity (the probability of this trigger phrase having an effect relative to the global affinity variable)
        * List of responses
            * First response object
                * Phrase to send
                * Number of tickets the phrase is worth (in the lottery system where each response adds the number of
                  tickets it was given here and only one is chosen at random)
            * Second response object
                * ...
            * ...
    * Second trigger object
        * ...
    * ...
    
Here's an example of how this can look in practice:
```json
[
  {
    "triggerPhrase": "bot",
    "affinity": 30,
    "responses": [
      {
        "output": "That's me!",
        "tickets": 3
      }, 
      {
        "output": "You're a bot!",
        "tickets": 1
      }
    ]
  },
  {
    "triggerPhrase": "wow",
    "affinity": 60,
    "responses": [
      {
        "output": "lame",
        "tickets": 5
      }
    ]
  },
  {
    "triggerPhrase": "eek",
    "affinity": 10,
    "responses": [
      {
        "output": "interesting",
        "tickets": 1
      }
    ]
  }
]
```
In this example, assuming that the global affinity is set to 100, a message containing the substring "bot" will trigger a response 30% of the time. With a global affinity of 200, every trigger phrase's probability of triggering would be halved. 

If the phrase "bot" appears, and a response is triggered, three tickets containing the message "That's me!" are entered into a "hat". One ticket with "You're a bot!" is entered. Each ticket has an equal probability of being selected, meaning that "You're a bot!" will only appear a quarter of the time. Since the phrases "wow" and "eek" each only correspond to one response, the number of tickets for each is irrelevant. 

Another behavior worth noting is that the bot shuffles the list of trigger phrases on each message received. As a result, the order of phrases in the data file is irrelevant. 