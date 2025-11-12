# Evaluation lab - Akka

## Group number: 49

## Group members

- Student 1 : Marco Romero Molina
- Student 2 : Matias Chialva
- Student 3 : Seyedmohammad Tabatabaei


The relation between de balancer and the actors was made using the method of the splitFirstLetter() for putQuery and it always send the the primary and the replicated to the same worker. Then the Get is made waiting time to responding , if one of them response but does not have the data, the balancer asks the other one, if any of them response the balancer sends a TimeOutMsg.

The client manages 3 types of messages and manages the communication with the balancer using queries to put the emails. It does not know anything about the workers, there's an abstraction of them while using the balancer. 
