# distributed-file-system
#### A simple distributed system for sharing files accros multiple farms &amp; subfarms


  * This is setup for a maximum 1 Client, 1 Middleware, 3 Remote Managers, 9 Workers
  
  * The Middleware works almost like a centralized server. If it dies, nothing goes thru. 
    * Any new connection must go thru it. 
  
  * The Remote Manager oversees all the workers
    * Only 3 RM can connect at one time
    * Once an RM connects, it automatically updates its directory to match that of the most up to date RM
      * This only happens if once all its workers have connected.
      * If it is an RM that was previously dead, the workers will connect automatically.
  
  * The Worker is responsible for servicing the client's requests
    * A client can either upload or download a file.
      * A request for a non existing file will be rejected.
        * THe client is notified of this.
    * On upload services:
      * The worker automatically updates itself and the other workers in its subfarm.
        * This is done thru a MC chat (a thread is always listening for new files)
      * The worker also blasts the file out to its RM
        * The RM in there blasts it out to the other RM
          * This is also done thru MC but only the RMs are listening in.
          * Each RM then blasts the file to the subfarms's MC
    
    * A version of each file is saved in a hashmap to prevent copying duplicate data accross multiple farms.
    * A farm is defined as a group of RM (Only 1 is allowed)
      * They never communicate with clients
      * They are allowed to communicate w/in themselves
    * A subfarm is a group of Workers (Only 3 are allowed)
      * Workers can talk to others in its subfarm
      * They cannot hop subfarms (aka talk to workers in other subfarms)
