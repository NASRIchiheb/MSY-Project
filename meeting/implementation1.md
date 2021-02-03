# Meeting Implementation 1

## Meeting 1
- Date: 14/11/2020
- Time: from 14:00 to 14:40
- Length: 40min
- Place: MS Teams
- Main goal: organize implementation 1, distribute task
- Topics covered: Git workflow, Jade
- Task distribution and deadlines: [see on Trello](https://trello.com/b/4oHs7KwJ/msy-activity-1)

We started the meeting sharing our comprehension of the Implementation assignment. Then we decided divide the assignment as follow: three people have to focus more on code, one person focus more on pdf report, and one person has to coordinate the group and be the bridge between coders and who make the report. After a brief recap on how works the Git Workflow, we decided to divide the assignment into three subtask: 
- first subtask: code the three  basic agents
- second subtask: upgrade each agent with the "parse" function
- third subtask: upgrade beahviour and communication for each agent

We decided to meet on Sunday evening to confront the results of the first subtask.

## Meeting 2
- Date: 15/11/2020
- Time: from 19:30 to 22:50 
- Length: 2h 50min
- Place: MS Teams
- Main goal: merge all agents from first subtask, undestand basic functionality of Jade
- Topics covered: Git, agents setup, Jade architecture
- Task distribution and deadlines: [see on Trello](https://trello.com/b/4oHs7KwJ/msy-activity-1)

We started the meeting trying to merge all basic agents created in the master branch. This job stole a lot of time due to Eclipse and IntelliJ IDEA workspace configuration. Then we focused on the Jade Architecture, such as Containers (in particular Main Container because our application is not distributed) and the default agents active in the Main Container, such as AMS, DF and RMA (the "gui"). We decided to set uo another two meetings to track our progress, on Monday and on Tuesday. We decided also to start the second subtask coding the "parse" function that each agent has: UserAgent parse the user input, ManagerAgent parse the configuration file, FuzzyAgent parse the fuzzy configutation file. Finally we agreed on the structure of the pdf report.

## Meeting 3
- Date: 16/11/2020
- Time: from 20:00 to 21:55
- Length: 1h 55min
- Place: MS Teams
- Main goal: merge all agents from the stating point of the second subtask, understand agents behaviour 
- Topics covered: Git, agents behaviour, Jade architecture
- Task distribution and deadlines: [see on Trello](https://trello.com/b/4oHs7KwJ/msy-activity-1)

In this meeting we focused on merging the work done so far into the master branch, in order to achieve a common repository for building next features. We started experimenting with Agent Behaviours, in particular OneShotBehaviour and CyclicBehaviour. For keeping the code more clean we decided to code agents and behaviour in separated files. We also checked the "communication graph" for our MAS, so the UserAgent exchange messages with the ManagerAgent, which exchange messages with all the Fuzzy Agents. We decided to meet on Tuesday to track our progress regarding the "messages exchange" part which is the third and final subtask.

## Meeting 4
- Date: 17/11/2020
- Time: from 18:30 to 20:55
- Length: 2h 25min
- Place: MS Teams
- Main goal: merge all the branches, resolve agents communications problems
- Topics covered: Git, agents messages, Jade architecture
- Task distribution and deadlines: [see on Trello](https://trello.com/b/4oHs7KwJ/msy-activity-1)

In this meeting we merged all the branches, to achieve a final branch with all the work. We discuss about ACLmessages and how orchestrate the exchange of message between the agents. We have also spent some time to check if our project respected the metrics of the implementation assignment.

---