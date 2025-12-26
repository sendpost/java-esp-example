package sendpost.example;

import sendpost_java_sdk.ApiClient;
import sendpost_java_sdk.ApiException;
import sendpost_java_sdk.Configuration;
import sendpost_java_sdk.auth.ApiKeyAuth;
import sendpost_java_sdk.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive SendPost Java SDK Example for Email Service Providers (ESPs)
 * 
 * This example demonstrates a complete workflow that an ESP would typically follow:
 * 1. Create sub-accounts for different clients or use cases
 * 2. Set up webhooks to receive email event notifications
 * 3. Add and verify sending domains
 * 4. Send transactional and marketing emails
 * 5. Retrieve message details for tracking and debugging
 * 6. Monitor statistics via IPs and IP pools
 * 7. Manage IP pools for better deliverability control
 * 
 * To run this example:
 * 1. Set environment variables:
 *    - SENDPOST_SUB_ACCOUNT_API_KEY: Your sub-account API key
 *    - SENDPOST_ACCOUNT_API_KEY: Your account API key
 * 2. Or modify the API_KEY constants below
 * 3. Update email addresses and domain names with your verified values
 * 4. Run: mvn compile exec:java
 */
public class ESPExample {
    
    // API Configuration
    private static final String BASE_PATH = "https://api.sendpost.io/api/v1";
    
    // API Keys - Set these or use environment variables
    private static final String SUB_ACCOUNT_API_KEY = System.getenv("SENDPOST_SUB_ACCOUNT_API_KEY") != null 
        ? System.getenv("SENDPOST_SUB_ACCOUNT_API_KEY") 
        : "YOUR_SUB_ACCOUNT_API_KEY_HERE";
    
    private static final String ACCOUNT_API_KEY = System.getenv("SENDPOST_ACCOUNT_API_KEY") != null 
        ? System.getenv("SENDPOST_ACCOUNT_API_KEY") 
        : "YOUR_ACCOUNT_API_KEY_HERE";
    
    // Configuration - Update these with your values
    private static final String TEST_FROM_EMAIL = "from@yourdomain.com";
    private static final String TEST_TO_EMAIL = "to@example.com";
    private static final String TEST_DOMAIN_NAME = "yourdomain.com";
    private static final String WEBHOOK_URL = "https://your-webhook-endpoint.com/webhook";
    
    private ApiClient apiClient;
    private Integer createdSubAccountId = null;
    private String createdSubAccountApiKey = null;
    private Integer createdWebhookId = null;
    private String createdDomainId = null;
    private Integer createdIPPoolId = null;
    private String createdIPPoolName = null;
    private String sentMessageId = null;
    
    public ESPExample() {
        // Initialize API client
        apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath(BASE_PATH);
    }
    
    /**
     * Configure sub-account authentication
     */
    private void configureSubAccountAuth() {
        ApiKeyAuth subAccountAuth = (ApiKeyAuth) apiClient.getAuthentication("subAccountAuth");
        subAccountAuth.setApiKey(SUB_ACCOUNT_API_KEY);
    }
    
    /**
     * Configure account authentication
     */
    private void configureAccountAuth() {
        ApiKeyAuth accountAuth = (ApiKeyAuth) apiClient.getAuthentication("accountAuth");
        accountAuth.setApiKey(ACCOUNT_API_KEY);
    }
    
    /**
     * Step 1: Create a new sub-account
     * Sub-accounts allow you to segregate email sending by client, product, or use case
     */
    public void createSubAccount() {
        System.out.println("\n=== Step 1: Creating Sub-Account ===");
        
        try {
            configureAccountAuth();
            SubAccountApi subAccountApi = new SubAccountApi(apiClient);
            
            // Create new sub-account request
            CreateSubAccountRequest createSubAccountRequest = new CreateSubAccountRequest();
            createSubAccountRequest.setName("ESP Client - " + System.currentTimeMillis());
            
            System.out.println("Creating sub-account: " + createSubAccountRequest.getName());
            
            SubAccount subAccount = subAccountApi.createSubAccount(createSubAccountRequest);
            
            createdSubAccountId = subAccount.getId();
            createdSubAccountApiKey = subAccount.getApiKey();
            
            System.out.println("✓ Sub-account created successfully!");
            System.out.println("  ID: " + createdSubAccountId);
            System.out.println("  Name: " + subAccount.getName());
            System.out.println("  API Key: " + createdSubAccountApiKey);
            System.out.println("  Type: " + (subAccount.getType() != null && subAccount.getType().getValue() != null && subAccount.getType().getValue() == 1 ? "Plus" : "Regular"));
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to create sub-account:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 2: List all sub-accounts
     * Useful for managing multiple clients or use cases
     */
    public void listSubAccounts() {
        System.out.println("\n=== Step 2: Listing All Sub-Accounts ===");
        
        try {
            configureAccountAuth();
            SubAccountApi subAccountApi = new SubAccountApi(apiClient);
            
            System.out.println("Retrieving all sub-accounts...");
            List<SubAccount> subAccounts = subAccountApi.getAllSubAccounts(null, null, null);
            
            System.out.println("✓ Retrieved " + subAccounts.size() + " sub-account(s)");
            for (SubAccount subAccount : subAccounts) {
                System.out.println("  - ID: " + subAccount.getId());
                System.out.println("    Name: " + subAccount.getName());
                System.out.println("    API Key: " + subAccount.getApiKey());
                System.out.println("    Type: " + (subAccount.getType() != null && subAccount.getType().getValue() != null && subAccount.getType().getValue() == 1 ? "Plus" : "Regular"));
                System.out.println("    Blocked: " + (subAccount.getBlocked() != null && subAccount.getBlocked() ? "Yes" : "No"));
                if (subAccount.getCreated() != null) {
                    System.out.println("    Created: " + subAccount.getCreated());
                }
                System.out.println();
                
                // Use first sub-account if none selected
                if (createdSubAccountId == null && subAccount.getId() != null) {
                    createdSubAccountId = subAccount.getId();
                    createdSubAccountApiKey = subAccount.getApiKey();
                }
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to list sub-accounts:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 3: Create a webhook
     * Webhooks allow you to receive real-time notifications about email events
     */
    public void createWebhook() {
        System.out.println("\n=== Step 3: Creating Webhook ===");
        
        try {
            configureAccountAuth();
            WebhookApi webhookApi = new WebhookApi(apiClient);
            
            // Create new webhook
            CreateWebhookRequest createWebhookRequest = new CreateWebhookRequest();
            createWebhookRequest.setUrl(WEBHOOK_URL);
            createWebhookRequest.setEnabled(true);
            
            // Configure which events to receive
            createWebhookRequest.setProcessed(true);      // Email processed
            createWebhookRequest.setDelivered(true);       // Email delivered
            createWebhookRequest.setDropped(true);        // Email dropped
            createWebhookRequest.setSoftBounced(true);    // Soft bounce
            createWebhookRequest.setHardBounced(true);     // Hard bounce
            createWebhookRequest.setOpened(true);          // Email opened
            createWebhookRequest.setClicked(true);         // Link clicked
            createWebhookRequest.setUnsubscribed(true);    // Unsubscribed
            createWebhookRequest.setSpam(true);            // Marked as spam
            
            System.out.println("Creating webhook...");
            System.out.println("  URL: " + createWebhookRequest.getUrl());
            
            Webhook webhook = webhookApi.createWebhook(createWebhookRequest);
            createdWebhookId = webhook.getId();
            
            System.out.println("✓ Webhook created successfully!");
            System.out.println("  ID: " + createdWebhookId);
            System.out.println("  URL: " + webhook.getUrl());
            System.out.println("  Enabled: " + webhook.getEnabled());
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to create webhook:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 4: List all webhooks
     */
    public void listWebhooks() {
        System.out.println("\n=== Step 4: Listing All Webhooks ===");
        
        try {
            configureAccountAuth();
            WebhookApi webhookApi = new WebhookApi(apiClient);
            
            System.out.println("Retrieving all webhooks...");
            List<Webhook> webhooks = webhookApi.getAllWebhooks(null, null, null);
            
            System.out.println("✓ Retrieved " + webhooks.size() + " webhook(s)");
            for (Webhook webhook : webhooks) {
                System.out.println("  - ID: " + webhook.getId());
                System.out.println("    URL: " + webhook.getUrl());
                System.out.println("    Enabled: " + webhook.getEnabled());
                System.out.println();
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to list webhooks:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 5: Add a sending domain
     * Domains must be verified before they can be used for sending
     */
    public void addDomain() {
        System.out.println("\n=== Step 5: Adding Domain ===");
        
        try {
            configureSubAccountAuth();
            DomainApi domainApi = new DomainApi(apiClient);
            
            // Create domain request
            CreateDomainRequest domainRequest = new CreateDomainRequest();
            domainRequest.setName(TEST_DOMAIN_NAME);
            
            System.out.println("Adding domain: " + TEST_DOMAIN_NAME);
            
            Domain domain = domainApi.subaccountDomainPost(domainRequest);
            createdDomainId = domain.getId() != null ? domain.getId().toString() : null;
            
            System.out.println("✓ Domain added successfully!");
            System.out.println("  ID: " + createdDomainId);
            System.out.println("  Domain: " + domain.getName());
            System.out.println("  Verified: " + (domain.getVerified() != null && domain.getVerified() ? "Yes" : "No"));
            
            if (domain.getDkim() != null) {
                System.out.println("  DKIM Record: " + domain.getDkim().getTextValue());
            }
            
            System.out.println("\n⚠️  IMPORTANT: Add the DNS records shown above to your domain's DNS settings to verify the domain.");
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to add domain:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 6: List all domains
     */
    public void listDomains() {
        System.out.println("\n=== Step 6: Listing All Domains ===");
        
        try {
            configureSubAccountAuth();
            DomainApi domainApi = new DomainApi(apiClient);
            
            System.out.println("Retrieving all domains...");
            List<Domain> domains = domainApi.getAllDomains(null, null, null);
            
            System.out.println("✓ Retrieved " + domains.size() + " domain(s)");
            for (Domain domain : domains) {
                System.out.println("  - ID: " + domain.getId());
                System.out.println("    Domain: " + domain.getName());
                System.out.println("    Verified: " + (domain.getVerified() != null && domain.getVerified() ? "Yes" : "No"));
                System.out.println();
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to list domains:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 7: Send a transactional email
     * Transactional emails are typically triggered by user actions
     */
    public void sendTransactionalEmail() {
        System.out.println("\n=== Step 7: Sending Transactional Email ===");
        
        try {
            configureSubAccountAuth();
            EmailApi emailApi = new EmailApi(apiClient);
            
            // Create email message
            EmailMessageObject emailMessage = new EmailMessageObject();
            
            // Set sender
            EmailAddress from = new EmailAddress();
            from.setEmail(TEST_FROM_EMAIL);
            from.setName("Your Company");
            emailMessage.setFrom(from);
            
            // Set recipient
            List<Recipient> recipients = new ArrayList<>();
            Recipient recipient = new Recipient();
            recipient.setEmail(TEST_TO_EMAIL);
            recipient.setName("Customer");
            recipients.add(recipient);
            emailMessage.setTo(recipients);
            
            // Set email content
            emailMessage.setSubject("Order Confirmation - Transactional Email");
            emailMessage.setHtmlBody("<h1>Thank you for your order!</h1><p>Your order has been confirmed and will be processed shortly.</p>");
            emailMessage.setTextBody("Thank you for your order! Your order has been confirmed and will be processed shortly.");
            
            // Enable tracking
            emailMessage.setTrackOpens(true);
            emailMessage.setTrackClicks(true);
            
            // Add custom headers for tracking
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("X-Order-ID", "12345");
            headers.put("X-Email-Type", "transactional");
            emailMessage.setHeaders(headers);
            
            // Use IP pool if available
            if (createdIPPoolName != null && !createdIPPoolName.isEmpty()) {
                emailMessage.setIppool(createdIPPoolName);
                System.out.println("  Using IP Pool: " + createdIPPoolName);
            }
            
            // Add custom fields
            Map<String, String> customFields = new HashMap<String, String>();
            customFields.put("customer_id", "67890");
            customFields.put("order_value", "99.99");
            Map<String, Object> customFieldsObj = new HashMap<String, Object>();
            for (Map.Entry<String, String> entry : customFields.entrySet()) {
                customFieldsObj.put(entry.getKey(), entry.getValue());
            }
            recipient.setCustomFields(customFieldsObj);
            
            System.out.println("Sending transactional email...");
            System.out.println("  From: " + TEST_FROM_EMAIL);
            System.out.println("  To: " + TEST_TO_EMAIL);
            System.out.println("  Subject: " + emailMessage.getSubject());
            
            List<EmailResponse> responses = emailApi.sendEmail(emailMessage);
            
            if (!responses.isEmpty()) {
                EmailResponse response = responses.get(0);
                sentMessageId = response.getMessageId();
                
                System.out.println("✓ Transactional email sent successfully!");
                System.out.println("  Message ID: " + sentMessageId);
                System.out.println("  To: " + response.getTo());
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to send email:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 8: Send a marketing email
     * Marketing emails are typically sent to multiple recipients
     */
    public void sendMarketingEmail() {
        System.out.println("\n=== Step 8: Sending Marketing Email ===");
        
        try {
            configureSubAccountAuth();
            EmailApi emailApi = new EmailApi(apiClient);
            
            // Create email message
            EmailMessageObject emailMessage = new EmailMessageObject();
            
            // Set sender
            EmailAddress from = new EmailAddress();
            from.setEmail(TEST_FROM_EMAIL);
            from.setName("Marketing Team");
            emailMessage.setFrom(from);
            
            // Set multiple recipients
            List<Recipient> recipients = new ArrayList<>();
            Recipient recipient1 = new Recipient();
            recipient1.setEmail(TEST_TO_EMAIL);
            recipient1.setName("Customer 1");
            recipients.add(recipient1);
            
            // Add CC recipients if needed
            // List<Recipient> ccRecipients = new ArrayList<>();
            // Recipient ccRecipient = new Recipient();
            // ccRecipient.setEmail("cc@example.com");
            // ccRecipients.add(ccRecipient);
            // emailMessage.setCc(ccRecipients);
            
            // Set email content
            emailMessage.setSubject("Special Offer - 20% Off Everything!");
            emailMessage.setHtmlBody(
                "<html><body>" +
                "<h1>Special Offer!</h1>" +
                "<p>Get 20% off on all products. Use code: <strong>SAVE20</strong></p>" +
                "<p><a href=\"https://example.com/shop\">Shop Now</a></p>" +
                "</body></html>"
            );
            emailMessage.setTextBody("Special Offer! Get 20% off on all products. Use code: SAVE20. Visit: https://example.com/shop");
            
            // Enable tracking
            emailMessage.setTrackOpens(true);
            emailMessage.setTrackClicks(true);
            
            // Add group for analytics
            List<String> groups = new ArrayList<>();
            groups.add("marketing");
            groups.add("promotional");
            emailMessage.setGroups(groups);
            
            // Add custom headers
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("X-Email-Type", "marketing");
            headers.put("X-Campaign-ID", "campaign-001");
            emailMessage.setHeaders(headers);
            
            // Use IP pool if available
            if (createdIPPoolName != null && !createdIPPoolName.isEmpty()) {
                emailMessage.setIppool(createdIPPoolName);
                System.out.println("  Using IP Pool: " + createdIPPoolName);
            }
            
            System.out.println("Sending marketing email...");
            System.out.println("  From: " + TEST_FROM_EMAIL);
            System.out.println("  To: " + TEST_TO_EMAIL);
            System.out.println("  Subject: " + emailMessage.getSubject());
            
            List<EmailResponse> responses = emailApi.sendEmail(emailMessage);
            
            if (!responses.isEmpty()) {
                EmailResponse response = responses.get(0);
                if (sentMessageId == null) {
                    sentMessageId = response.getMessageId();
                }
                
                System.out.println("✓ Marketing email sent successfully!");
                System.out.println("  Message ID: " + response.getMessageId());
                System.out.println("  To: " + response.getTo());
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to send email:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 9: Retrieve message details
     * Useful for tracking, debugging, and customer support
     */
    public void getMessageDetails() {
        System.out.println("\n=== Step 9: Retrieving Message Details ===");
        
        if (sentMessageId == null) {
            System.err.println("✗ No message ID available. Please send an email first.");
            return;
        }
        
        try {
            configureAccountAuth();
            MessageApi messageApi = new MessageApi(apiClient);
            
            System.out.println("Retrieving message with ID: " + sentMessageId);
            
            Message message = messageApi.getMessageById(sentMessageId);
            
            System.out.println("✓ Message retrieved successfully!");
            System.out.println("  Message ID: " + message.getMessageID());
            System.out.println("  Account ID: " + message.getAccountID());
            System.out.println("  Sub-Account ID: " + message.getSubAccountID());
            System.out.println("  IP ID: " + message.getIpID());
            System.out.println("  Public IP: " + message.getPublicIP());
            System.out.println("  Local IP: " + message.getLocalIP());
            System.out.println("  Email Type: " + message.getEmailType());
            
            if (message.getSubmittedAt() != null) {
                System.out.println("  Submitted At: " + message.getSubmittedAt());
            }
            
            if (message.getFrom() != null) {
                System.out.println("  From: " + (message.getFrom().getEmail() != null ? message.getFrom().getEmail() : "N/A"));
            }
            
            if (message.getTo() != null) {
                System.out.println("  To: " + (message.getTo().getEmail() != null ? message.getTo().getEmail() : "N/A"));
                if (message.getTo().getName() != null) {
                    System.out.println("    Name: " + message.getTo().getName());
                }
            }
            
            if (message.getSubject() != null) {
                System.out.println("  Subject: " + message.getSubject());
            }
            
            if (message.getIpPool() != null && !message.getIpPool().isEmpty()) {
                System.out.println("  IP Pool: " + message.getIpPool());
            }
            
            if (message.getAttempt() != null) {
                System.out.println("  Delivery Attempts: " + message.getAttempt());
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to get message:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 10: Get sub-account statistics
     * Monitor email performance metrics
     */
    public void getSubAccountStats() {
        System.out.println("\n=== Step 10: Getting Sub-Account Statistics ===");
        
        if (createdSubAccountId == null) {
            System.err.println("✗ No sub-account ID available. Please create or list sub-accounts first.");
            return;
        }
        
        try {
            configureAccountAuth();
            StatsApi statsApi = new StatsApi(apiClient);
            
            // Get stats for the last 7 days
            LocalDate toDate = LocalDate.now();
            LocalDate fromDate = toDate.minusDays(7);
            
            System.out.println("Retrieving stats for sub-account ID: " + createdSubAccountId);
            System.out.println("  From: " + fromDate);
            System.out.println("  To: " + toDate);
            
            List<Stat> stats = statsApi.accountSubaccountStatSubaccountIdGet(fromDate, toDate, createdSubAccountId.longValue());
            
            System.out.println("✓ Stats retrieved successfully!");
            System.out.println("  Retrieved " + stats.size() + " stat record(s)");
            
            long totalProcessed = 0;
            long totalDelivered = 0;
            
            for (Stat stat : stats) {
                System.out.println("\n  Date: " + stat.getDate());
                if (stat.getStat() != null) {
                    StatStat statData = stat.getStat();
                    System.out.println("    Processed: " + statData.getProcessed());
                    System.out.println("    Delivered: " + statData.getDelivered());
                    System.out.println("    Dropped: " + statData.getDropped());
                    System.out.println("    Hard Bounced: " + statData.getHardBounced());
                    System.out.println("    Soft Bounced: " + statData.getSoftBounced());
                    System.out.println("    Unsubscribed: " + statData.getUnsubscribed());
                    System.out.println("    Spam: " + statData.getSpam());
                    
                    totalProcessed += statData.getProcessed() != null ? statData.getProcessed() : 0;
                    totalDelivered += statData.getDelivered() != null ? statData.getDelivered() : 0;
                }
            }
            
            System.out.println("\n  Summary (Last 7 days):");
            System.out.println("    Total Processed: " + totalProcessed);
            System.out.println("    Total Delivered: " + totalDelivered);
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to get stats:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 11: Get aggregate statistics
     * Get overall performance metrics
     */
    public void getAggregateStats() {
        System.out.println("\n=== Step 11: Getting Aggregate Statistics ===");
        
        if (createdSubAccountId == null) {
            System.err.println("✗ No sub-account ID available. Please create or list sub-accounts first.");
            return;
        }
        
        try {
            configureAccountAuth();
            StatsApi statsApi = new StatsApi(apiClient);
            
            // Get aggregate stats for the last 7 days
            LocalDate toDate = LocalDate.now();
            LocalDate fromDate = toDate.minusDays(7);
            
            System.out.println("Retrieving aggregate stats for sub-account ID: " + createdSubAccountId);
            System.out.println("  From: " + fromDate);
            System.out.println("  To: " + toDate);
            
            AggregateStat aggregateStat = statsApi.accountSubaccountStatSubaccountIdAggregateGet(
                fromDate, toDate, createdSubAccountId.longValue());
            
            System.out.println("✓ Aggregate stats retrieved successfully!");
            System.out.println("  Processed: " + aggregateStat.getProcessed());
            System.out.println("  Delivered: " + aggregateStat.getDelivered());
            System.out.println("  Dropped: " + aggregateStat.getDropped());
            System.out.println("  Hard Bounced: " + aggregateStat.getHardBounced());
            System.out.println("  Soft Bounced: " + aggregateStat.getSoftBounced());
            System.out.println("  Unsubscribed: " + aggregateStat.getUnsubscribed());
            System.out.println("  Spam: " + aggregateStat.getSpam());
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to get aggregate stats:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 12: List all IPs
     * Monitor your dedicated IP addresses
     */
    public void listIPs() {
        System.out.println("\n=== Step 12: Listing All IPs ===");
        
        try {
            configureAccountAuth();
            IpApi ipApi = new IpApi(apiClient);
            
            System.out.println("Retrieving all IPs...");
            List<IP> ips = ipApi.getAllIps(null, null, null);
            
            System.out.println("✓ Retrieved " + ips.size() + " IP(s)");
            for (IP ip : ips) {
                System.out.println("  - ID: " + ip.getId());
                System.out.println("    IP Address: " + ip.getPublicIP());
                if (ip.getReverseDNSHostname() != null) {
                    System.out.println("    Reverse DNS: " + ip.getReverseDNSHostname());
                }
                if (ip.getCreated() != null) {
                    System.out.println("    Created: " + ip.getCreated());
                }
                System.out.println();
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to list IPs:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 13: Create an IP Pool
     * IP pools allow you to group IPs for better deliverability control
     */
    public void createIPPool() {
        System.out.println("\n=== Step 13: Creating IP Pool ===");
        
        try {
            configureAccountAuth();
            IpPoolsApi ipPoolsApi = new IpPoolsApi(apiClient);
            
            // First, get available IPs
            IpApi ipApi = new IpApi(apiClient);
            List<IP> ips = ipApi.getAllIps(null, null, null);
            
            if (ips.isEmpty()) {
                System.out.println("⚠️  No IPs available. Please allocate IPs first.");
                return;
            }
            
            // Create IP pool request
            IPPoolCreateRequest poolRequest = new IPPoolCreateRequest();
            poolRequest.setName("Marketing Pool " + System.currentTimeMillis());
            poolRequest.setRoutingStrategy(0); // 0 = RoundRobin, 1 = EmailProviderStrategy
            
            // Add IPs to the pool (convert IP to EIP)
            List<EIP> poolIPs = new ArrayList<>();
            // Add first available IP (you can add more)
            if (!ips.isEmpty()) {
                EIP eip = new EIP();
                eip.setPublicIP(ips.get(0).getPublicIP());
                poolIPs.add(eip);
            }
            poolRequest.setIps(poolIPs);
            
            // Set warmup interval (required, must be > 0)
            poolRequest.setWarmupInterval(24); // 24 hours
            
            // Set overflow strategy (0 = None, 1 = Use overflow pool)
            poolRequest.setOverflowStrategy(0);
            
            System.out.println("Creating IP pool: " + poolRequest.getName());
            System.out.println("  Routing Strategy: Round Robin");
            System.out.println("  IPs: " + poolIPs.size());
            System.out.println("  Warmup Interval: " + poolRequest.getWarmupInterval() + " hours");
            
            IPPool ipPool = ipPoolsApi.createIPPool(poolRequest);
            createdIPPoolId = ipPool.getId();
            if (ipPool.getName() != null) {
                createdIPPoolName = ipPool.getName();
            }
            
            System.out.println("✓ IP pool created successfully!");
            System.out.println("  ID: " + createdIPPoolId);
            System.out.println("  Name: " + ipPool.getName());
            System.out.println("  Routing Strategy: " + ipPool.getRoutingStrategy());
            System.out.println("  IPs in pool: " + (ipPool.getIps() != null ? ipPool.getIps().size() : 0));
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to create IP pool:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 14: List all IP Pools
     */
    public void listIPPools() {
        System.out.println("\n=== Step 14: Listing All IP Pools ===");
        
        try {
            configureAccountAuth();
            IpPoolsApi ipPoolsApi = new IpPoolsApi(apiClient);
            
            System.out.println("Retrieving all IP pools...");
            List<IPPool> ipPools = ipPoolsApi.getAllIPPools(null, null, null);
            
            System.out.println("✓ Retrieved " + ipPools.size() + " IP pool(s)");
            for (IPPool ipPool : ipPools) {
                System.out.println("  - ID: " + ipPool.getId());
                System.out.println("    Name: " + ipPool.getName());
                System.out.println("    Routing Strategy: " + ipPool.getRoutingStrategy());
                System.out.println("    IPs in pool: " + (ipPool.getIps() != null ? ipPool.getIps().size() : 0));
                if (ipPool.getIps() != null && !ipPool.getIps().isEmpty()) {
                    for (IP ip : ipPool.getIps()) {
                        System.out.println("      - " + ip.getPublicIP());
                    }
                }
                System.out.println();
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to list IP pools:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Step 15: Get account-level statistics
     * Overall statistics across all sub-accounts
     */
    public void getAccountStats() {
        System.out.println("\n=== Step 15: Getting Account-Level Statistics ===");
        
        try {
            configureAccountAuth();
            StatsAApi statsAApi = new StatsAApi(apiClient);
            
            // Get stats for the last 7 days
            LocalDate toDate = LocalDate.now();
            LocalDate fromDate = toDate.minusDays(7);
            
            System.out.println("Retrieving account-level stats...");
            System.out.println("  From: " + fromDate);
            System.out.println("  To: " + toDate);
            
            List<AccountStats> accountStats = statsAApi.getAllAccountStats(fromDate, toDate);
            
            System.out.println("✓ Account stats retrieved successfully!");
            System.out.println("  Retrieved " + accountStats.size() + " stat record(s)");
            
            for (AccountStats stat : accountStats) {
                System.out.println("\n  Date: " + stat.getDate());
                if (stat.getStat() != null) {
                    AccountStatsStat statData = stat.getStat();
                    System.out.println("    Processed: " + statData.getProcessed());
                    System.out.println("    Delivered: " + statData.getDelivered());
                    System.out.println("    Dropped: " + statData.getDropped());
                    System.out.println("    Hard Bounced: " + statData.getHardBounced());
                    System.out.println("    Soft Bounced: " + statData.getSoftBounced());
                    System.out.println("    Opens: " + statData.getOpened());
                    System.out.println("    Clicks: " + statData.getClicked());
                    System.out.println("    Unsubscribed: " + statData.getUnsubscribed());
                    System.out.println("    Spams: " + statData.getSpam());
                }
            }
            
        } catch (ApiException e) {
            System.err.println("✗ Failed to get account stats:");
            System.err.println("  Status code: " + e.getCode());
            System.err.println("  Response body: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error:");
            e.printStackTrace();
        }
    }
    
    /**
     * Run the complete ESP workflow
     */
    public void runCompleteWorkflow() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║   SendPost Java SDK - ESP Example Workflow                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        
        // Step 1: List existing sub-accounts (or create new one)
        listSubAccounts();
        
        // Step 2: Create webhook for event notifications
        createWebhook();
        listWebhooks();
        
        // Step 3: Add and verify domain
        addDomain();
        listDomains();
        
        // Step 4: Manage IPs and IP pools (before sending emails)
        listIPs();
        createIPPool();
        listIPPools();
        
        // Step 5: Send emails (using the created IP pool)
        sendTransactionalEmail();
        sendMarketingEmail();
        
        // Step 6: Monitor statistics
        getSubAccountStats();
        getAggregateStats();
        
        // Step 7: Get account-level overview
        getAccountStats();
        
        // Step 8: Retrieve message details (at the end to give system time to store data)
        // Add a small delay to ensure message data is stored
        System.out.println("\n⏳ Waiting a few seconds for message data to be stored...");
        try {
            Thread.sleep(3000); // Wait 3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        getMessageDetails();
        
        System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║   Workflow Complete!                                          ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
    }
    
    /**
     * Main method
     */
    public static void main(String[] args) {
        ESPExample example = new ESPExample();
        
        // Check if API keys are set
        if (SUB_ACCOUNT_API_KEY.equals("YOUR_SUB_ACCOUNT_API_KEY_HERE") || 
            ACCOUNT_API_KEY.equals("YOUR_ACCOUNT_API_KEY_HERE")) {
            System.err.println("⚠️  WARNING: Please set your API keys!");
            System.err.println("   Set environment variables:");
            System.err.println("   - SENDPOST_SUB_ACCOUNT_API_KEY");
            System.err.println("   - SENDPOST_ACCOUNT_API_KEY");
            System.err.println("   Or modify the constants in ESPExample.java");
            System.err.println();
        }
        
        // Run the complete workflow
        example.runCompleteWorkflow();
    }
}
