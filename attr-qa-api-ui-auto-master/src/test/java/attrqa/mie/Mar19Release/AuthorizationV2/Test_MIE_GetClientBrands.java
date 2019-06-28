package attrqa.mie.Mar19Release.AuthorizationV2;

import attrqa.framework.base.APITestBase;
import attrqa.framework.dataProvider.TestDataProvider;
import attrqa.framework.helper.KeywordsHelper;
import attrqa.framework.helper.PropertiesHelper;
import attrqa.framework.helper.ValidationsHelper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test_MIE_GetClientBrands extends APITestBase {
    String sHost;
    String sEndpoint;

    @BeforeClass
    public void getEnvironment(ITestContext testContext) throws IOException {
        sHost = PropertiesHelper
                .getHostStringFromPropertiesFile(sPropertyFileName, Test_MIE_GetClientBrands.class);
        sEndpoint = "/v2/metadata/GetClientBrands";
    }

    @Test(dataProvider = "dataProvider", dataProviderClass = TestDataProvider.class)
    public void getClientBrands(Map<Object, Object> testData, ITestContext testContext)
            throws IOException {
        String contentType = KeywordsHelper
                .replaceKeyword(testData.get("contenttype").toString(), testContext);
        String clientKey = KeywordsHelper
                .replaceKeyword(testData.get("clientkey").toString(), testContext);
        String responseCode = KeywordsHelper
                .replaceKeyword(testData.get("responsecode").toString(), testContext);
        String successResponse = KeywordsHelper
                .replaceKeyword(testData.get("successresponse").toString(), testContext);
        String errorResponse = KeywordsHelper
                .replaceKeyword(testData.get("errorresponse").toString(), testContext);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", contentType);
        headerMap.put("ClientKey", clientKey);
        headerMap.put("Token", getSuiteContextValue("authorizationtoken_mie", testContext));



        RequestSpecification request = RestAssured.given().headers(headerMap);
        Response response = request.get(sHost + sEndpoint);

        logInfo("Endpoint URL: " + sHost + sEndpoint);


        boolean isResponseCodeCorrect = (response.getStatusCode() == Integer.parseInt(responseCode));
        assertTrue(isResponseCodeCorrect, "API response status code is  expected. \nExpected : " + Integer.parseInt(responseCode)
                        + "\nObserved : " + response.getStatusCode(),
                "API response status code is not as expected.\nExpected : " + Integer.parseInt(responseCode)
                        + "\nObserved : " + response.getStatusCode());

        String sJSON = response.body().print();
        if (String.valueOf(response.getStatusCode()).startsWith("2") && responseCode.startsWith("2")) {
            DocumentContext documentContext = JsonPath.parse(sJSON);
            String[] validationNodes = successResponse.split("~");
            for (int i = 0; i < validationNodes.length; i++) {
                String sValidationNode = validationNodes[i];
                String[] nodeValuePairs = sValidationNode.split(":");
                String sNode = nodeValuePairs[0];
                String sValue = nodeValuePairs[1];
                if (sValue.startsWith("[")) {
                    List<String> listOfExpectedValues = getStringAsList(sValue);
                    List<String> listOfObservedValues = documentContext.read(sNode);
                    boolean isExpectedListOfValuesPresent = listOfObservedValues.containsAll(listOfExpectedValues);
                    assertTrue(isExpectedListOfValuesPresent, "QueryKey : " + sNode + "\nExpectedValue : " + listOfExpectedValues + "\nObservedValue: " + listOfObservedValues, "QueryKey : " + sNode + "\nExpectedValue : " + listOfExpectedValues + "\nObservedValue : " + listOfObservedValues);
                } else {
                    String sExpectedValue = sValue;
                    if (sExpectedValue.trim().equalsIgnoreCase("null") || documentContext.read(sNode) == null) {
                        boolean isNull = sExpectedValue.equalsIgnoreCase(documentContext.read(sNode)==null?"null":documentContext.read(sNode).toString());
                        assertTrue(isNull, "QueryKey : " + sNode + "\nExpectedValue : " + sExpectedValue + "\nObservedValue: " + documentContext.read(sNode), "QueryKey : " + sNode + "\nExpectedValue : " + sExpectedValue + "\nObservedValue : " + documentContext.read(sNode));
                    } else {
                        String sObservedValue = documentContext.read(sNode).toString();
                        boolean isExpectedValuePresent = sExpectedValue.equalsIgnoreCase(sObservedValue);
                        assertTrue(isExpectedValuePresent, "QueryKey : " + sNode + "\nExpectedValue : " + sExpectedValue + "\nObservedValue : " + sObservedValue, "QueryKey : " + sNode + "\nExpectedValue : " + sExpectedValue + "\nObservedValue : " + sObservedValue);
                    }

                }
            }
        }


        if (!(errorResponse.trim().equals(""))) {
            String partsKey = errorResponse.substring(0, errorResponse.indexOf(":"));
            String partsValue = errorResponse.substring(errorResponse.indexOf(":") + 1);
            io.restassured.path.json.JsonPath jsonpath = response.jsonPath();
            String observedMessage = jsonpath.get(partsKey);
            String expectedMessage = partsValue;
            boolean isResponseMessageCorrect;
            if (errorResponse.contains("*")) {
                isResponseMessageCorrect = ValidationsHelper
                        .compareStringsWithWildcards(observedMessage, expectedMessage);
            } else {
                isResponseMessageCorrect = expectedMessage.equalsIgnoreCase(observedMessage);
            }
            assertTrue(isResponseMessageCorrect, "API response observedMessage is as expected. \nExpected : " + expectedMessage
                            + "\nObserved : " + observedMessage,
                    "API response observedMessage is not as expected. \nExpected : " + expectedMessage
                            + "\nObserved : " + observedMessage);
        }
    }

    private List<String> getStringAsList (String sExpectedValues){
        String[] sValues = sExpectedValues.replace("[", "").replace("]", "").split(",");
        return Arrays.asList(sValues);
    }


}
