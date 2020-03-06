# Assignment 3 - answer:
2 Critical bugs was found during the running test

# BUGS
## 1. User registration data type and format is not validated

1. Enter user registration data email, firstname and lastname in invalid format: 
- non-alphabetic values for firstname and lastname
- incorrect format of email
- empty values for all
2. Send user registration data via POST request to http://localhost:8080/customer/create
3. Check response status
4. make sure if the data is added

Expected: status 400 - bad request should be returned, user mustn't not be added
Actual: status 200 - OK is returned, user is added inspite of invalid data
(as no data is validated at all during registration, severity is Critical)


## 2. Unable to set Marketing Consent data to "true"

Preconditions: user User1 is registered with marketing consent "false" by default

1. Change marketing consent value to "true" via PUT request to http://localhost:8080/customer/create
2. Check response status
3. Make sure if the data got changed via GET request

Expected: status 200 - OK,bad request should be returned, user mustn't not be added
Actual: status 200 - OK is returned, but value is not changed
(as it impackts business because users cannot get marketing emails, severity is Critical)
