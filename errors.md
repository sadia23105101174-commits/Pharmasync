# AI error log
Format: Tool | What the agent did wrong | How I found it | How it was fixed
mvnw | Used incorrect record accessor quantityAllocated() instead of allocatedQuantity() on domain BatchAllocation | Maven compile failed with symbol error | Inspected record definition and updated caller to allocatedQuantity()
mvnw | Missing findByStatus(String status) in OutboxRepository without Pageable | Maven test-compile failed with method signature mismatch | Added findByStatus(String status) to OutboxRepository
mvnw | Test expected 'requires prescriber registration number' whereas service threw 'without prescriber registration number' | Test failure in BranchSaleServiceTest | Aligned assertion substring with service exception message
