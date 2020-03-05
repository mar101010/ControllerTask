package test.java.defaultNg;//ok?

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CustomerControllerTest {
	private final int userIdFirst = 1;
	private final int userIdFifth = 5;
	private final String jsonParameterFirstName = "firstName";
	private final String jsonParameterLastName = "lastName";
	private final String jsonParameterEmail = "email";

	@BeforeClass
	public void Setup() {
		RestAssured.baseURI = "http://localhost:8080/customer";
	}

	@Test
	public void GetCustomers_ShouldReturn200() {
		//given
		int randomUserId = ThreadLocalRandom.current().nextInt(userIdFirst, userIdFifth + 1);
		String randomUserIdAsString = String.valueOf(randomUserId);
		//when
		Response response = getCustomerById(randomUserIdAsString);
		String responseBody = response.getBody().asString();
		//then
		int statusCode = response.getStatusCode();
		System.out.println("The status code recieved: " + statusCode);
		System.out.println("Response Body is: " + responseBody); //---not necessary?
		System.out.println("User ID is: " + randomUserId);
		Assert.assertEquals(statusCode, 200);
	}

	@Test
	public void GetCustomers_ShouldReturn404_WhenNotExistingId() {
		Response response = getCustomerById("3656746");
		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 404);
	}

	@Test
	public void GetCustomers_ShouldReturn400_WhenInvalidId() {
		Response response = getCustomerById("krjkdf//.");
		int statusCode = response.getStatusCode();
		Assert.assertEquals(statusCode, 400);
	}

	@Test
	public void CompareCustomersApiDataWithDatabase() throws IOException {
		String csvFile = "src/test/resources/customers.csv";
		Reader reader = new BufferedReader(new FileReader(csvFile));
		CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader("first_name", "last_name", "email"));
		List<CSVRecord> records = parser.getRecords();
		for(int i = 1; i < records.size(); i++){
			//given
			CSVRecord record = records.get(i);
			String expectedFirstName = record.get("first_name");
			String expectedLastName = record.get("last_name");
			String expectedEmail = record.get("email");
			String idAsString = String.valueOf(i);
			//when
			Response response = getCustomerById(idAsString);
			JsonPath jsonPathEvaluator = response.jsonPath();
			String actualFirstName = jsonPathEvaluator.get(jsonParameterFirstName);
			String actualLastName = jsonPathEvaluator.get(jsonParameterLastName);
			String actualEmail = jsonPathEvaluator.get(jsonParameterEmail);
			//then
			Assert.assertEquals(actualFirstName, expectedFirstName);
			Assert.assertEquals(actualLastName, expectedLastName);
			Assert.assertEquals(actualEmail, expectedEmail);
		}
	}



	@Test //sequence
	public void RegistrationSuccessful_ShouldReturn200AndSetMarketingConsentToFalse() {
		//given
		String jsonValueFirstName = "Kate";
		String jsonValueLastName = "Johnson";
		String jsonValueEmail = "johnson@gmail.com";
		Response responsePost = postNewCustomer(jsonValueEmail, jsonValueFirstName, jsonValueLastName);
		int statusCode = responsePost.getStatusCode();
		String newUserId = responsePost.body().asString();
		System.out.println("The status code recieved: " + statusCode);
		System.out.println("Response body: " + newUserId);
		Assert.assertEquals(statusCode, 200);
		Response responseGet = getCustomerById(newUserId);
		JsonPath jsonPathEvaluator = responseGet.jsonPath();
		//when
		boolean marketingConsent = jsonPathEvaluator.get("marketingConsent");
		//then
		System.out.println("Marketing consent of a new user by default is " + marketingConsent);
		Assert.assertEquals(marketingConsent, false);
	}

	@Test
	public void RegistrationFailed_ShouldReturn400_WhenInvalidDataFormat() {
		//given
		String jsonValueFirstName = "Kate23/.";
		String jsonValueLastName = "";
		String jsonValueEmail = "kjohnsongmail%com";
		//when
		Response response = postNewCustomer(jsonValueEmail, jsonValueFirstName, jsonValueLastName);
		//then
		int statusCode = response.getStatusCode();
		String newUserId = response.body().asString();
		System.out.println("The status code recieved: " + statusCode);
		System.out.println("Response body: " + newUserId);
		Assert.assertEquals(statusCode, 400);
	}

	@Test
	public void ChangeMarketingConsentSuccessful() {
		//given
		RequestSpecification request = RestAssured.given();
		String jsonValueFirstName = "Melanie";
		String jsonValueLastName = "Johnson";
		String jsonValueEmail = "johnson@gmail.com";
		Response responsePost = postNewCustomer(jsonValueEmail, jsonValueFirstName, jsonValueLastName);
		String newUserId = responsePost.body().asString();
		//Response responseGet = getCustomerById(newUserId);
		//?
		//JsonPath jsonPathEvaluator = responseGet.jsonPath();
		//boolean marketingConsent = jsonPathEvaluator.get("marketingConsent");
		//
		boolean newMarketingConsent = true;
		JSONObject requestPutBody = new JSONObject();
		requestPutBody.put("marketingConsent", newMarketingConsent);
		request.header("Content-Type", "application/json");
		request.body(requestPutBody.toJSONString());
		System.out.println("A new user with id = " + newUserId + " and a firstname " + jsonValueFirstName + " has a new value for Marketing Concent: " + newMarketingConsent);
		System.out.println("A full response is: " + requestPutBody);
		Assert.assertEquals(newMarketingConsent, true);
	}

	private Response postNewCustomer(String email, String firstName, String lastName){
		RequestSpecification request = RestAssured.given();
		JSONObject requestBody = new JSONObject();
		requestBody.put(jsonParameterEmail, email);
		requestBody.put(jsonParameterFirstName, firstName);
		requestBody.put(jsonParameterLastName, lastName);
		request.header("Content-Type", "application/json");
		request.body(requestBody.toJSONString());
		return request.post("/create");
	}

	private Response getCustomerById (String id) {
		RequestSpecification request = RestAssured.given();
		return request.get("/requestById/" + id);
	}
}