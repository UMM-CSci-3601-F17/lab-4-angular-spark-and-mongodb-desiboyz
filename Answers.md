1. UserController creates an instance of a variable that calls the server to create and retrieve data from the Mongo database.

2. Iterating through the user collection to match the id passed to find a user. If the user with the given id is found, the id is converted and returned as json. Otherwise if no match is found, null is returned.

3. getUsers takes a parameter of map and checks if the given users are found by iterating through the data that match the passed ‘age’. If so, an object Document is created where all the matched ages are parsed to integers and added to the document. The document then iterates through the userCollection to match and return the users found with the same age. 

4. Documents represent the data of a user that is used within MongoDB. It is used to retrieve users based on any of the fields and it is also used for adding users to the DB. 

5. It clears the database and then populates it with an arraylist to do our work like testing etc.

6. Test to find users with age of 37 alongside the correct number of users with the given age.
