# Swarm intelligence based on pheromone trails with evaporation and diffusion.

This is a particles application written in [Scala.js](http://www.scala-js.org/) and is executed in browser.
The purpose of this application is the study chaotic behaviour of dynamic systems composed of agents that use pheromone
trails which evaporate and diffuse. The trails introduce feedback into the behaviour of the agents, which leads
to chaotic behaviour, when the long term system configuration is not computable given the initial conditions.
One of the ideas we want to check is that the similar feedback effect implemented by the means of the tape leads to the
Halting problem of the Turing machine, which resembles the chaotic behaviour (as there is no general way to determine
whether the given Turing machine is going to halt given the machine specification and the initial conditions).


This simulation is reproducing
[Sebastian Lague's results](https://www.youtube.com/watch?v=X-iSQQgOd1A&ab_channel=SebastianLague)
inspired by [Slackermanz' results](https://www.youtube.com/channel/UCmoNsNuM0M9VsIXfm2cHPiA/videos). The simulations
by both authors are similar to Conway's Game of Life and other Cellular Automata, some of which which are proven
to be Turing complete, like [Rule 110](https://en.wikipedia.org/wiki/Rule_110) for instance.


## Get started

To get started, run `sbt ~fastOptJS` in this example project. This should
download dependencies and prepare the relevant javascript files. If you open
`localhost:12345/target/scala-2.11/classes/index-dev.html` in your browser, it will show you particles simulation.
You can then edit the application and see the updates be sent live to the browser without needing to refresh the page.

## The optimized version

Run `sbt ~fullOptJS` and open up `index-opt.html` for an optimized (~200kb) version
of the final application, useful for final publication.
