# All-India-Grievance-Reporting-App 

# Published paper : https://www.irojournals.com/itdw/V2/I2/02.pdf

All India Grievance Redressal App is developed using Java and XML. In this app, Complaints are categorized into two type viz. Complaint Type and Complaint sub-type in which complaints are classified into multiple complaint types like Solid Waste Management, Drainage, Storm Water drain, Road and Traffic, Factories, Licence, Water Supplies and many more. Complaint sub-type is the further categorisation of the particular complaint type. . In order to place any complaint, user must be registered with the app by providing simple details like name, email, mobile number, username and password, etc. User data and complaint related data is stored in Firebase Realtime database.

I have removed *google_services.json* file. You can create your own firebase Project to generate it and include in this project.

Our App uses Email and Password based authentication mechanism. The user will select the category then the sub-category and then the user will be allowed to enter a description about the complaint and the user can also upload the image of the complaint and image will be stored in Firebase Storage which is a powerful, simple, and cost-effective object storage service. Glide, the most popular image loading library is used to download the image files in the application. Picasso is also used for the same but Glide is more memory efficient (about 8 MB) than Picasso (about 13 MB). The Picasso loads the full-size image into the memory and relies on GPU to resize that image to fit into the size of the ImageView. We will require less memory if we use While Glide, as it only loads an image that is priory resized with respect to ImageView, unlike Picasso than loading the full image and require more memory. This helps in preventing errors like OutOfMemoryError. The complaint location will be automatically captured. After successful submission of complaint, complaint ID will be auto-generated and this is the unique complaint id with which user can access the complaint details. The feature of automatic location capturing will avoid the Human type errors and also save the time of user.

# METHODOLOGY : 

With the help of android application, any user can file any type of Complaint across all the states of the India. Again, Complaints are classified into Complaint type and Complaint subtype. For example, Roads and Traffic is main Complaint type and under Roads and Traffic different sub-complaints have been included like Potholes, Street Light Problem, etc. Users will send Complaints from Mobile Application. Complaint site will be automatically captured which will avoid the Human type errors and save the Time of User. Workers of different states can enter the name of Region OR Zip code of that Region i.e. Vile Parle OR 400056 and check the Complaints received in particular Region of State. Workers will solve the complaint and Send the details of Solved Complaints including Image of Solved Complaint to the Admin. State Admin will receive notifications of Solved Complaints from Workers operating in that Region. After receiving the Notifications from Workers of Particular State, admin will verify the Complaint details provided by the worker and solve the Complaint. Admin can also communicate with workers of particular state in case if particular complaint is not solved by the worker from long time. When any state admin solves the complaint of particular user, an email containing complaint ID, Complaint status (Solved/Unsolved) and Complaint description will be sent to user. Users can search the complaints by using Complaint ID or Complaint Type in their Mobile through App. The main peculiarity of the app is that while registering any complaint, complaint location is automatically captured in the form of Latitude and Longitude co-ordinates. Geocoding Process is used to convert the location's latitude and longitude values into further details about an address and vice versa. 
         
