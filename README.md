# MIS-AMS

# Asset Management Service (AMS)

The **A**sset **M**anagement **S**ervice **(AMS)** is an interface module of the **Manufacturer Information Service (MIS)**.

| AMS is still under development. Contributions in form of requirements, issues and pull requests are highly welcome. |
|-----------------------------|

## AMS Overview

The **AMS** is a micro service providing a GraphQL interface to manage asset information, such as enterprises, factories, processes, manufacturing capabilities and many other types of information within the **Supplier Knowledge Base**. 
Among other data, manufacturers and On-Demand Manufacturing (ODM) platforms can register, update and delete their manufacturing capabilities via the AMS and are thus discoverable by the **Search Engine**. 

The **Asset Management Service's GraphQL API** is documented here: :blue_book: [AMS API](https://www.smartfactoryweb.de/assetmanagementservice/schema-doc/)

Sample requests as Postman collection: :blue_book: [Collection](https://www.smartfactoryweb.de/assetmanagementservice/requestcollection/AssetManagementService.postman_collection.json)

Classification of AMS based on the MIS architecture.

![AMS](/../main/docs/src/images/AMS.PNG)

The following use cases can be performed with the AMS.

### Direct Use Cases of AMS
> [!NOTE]
> Direct use of AMS via interfaces
3. UC3 **Requesting manufacturer information**
- Data consumer can use the MIS to query information such as production capabilities for a data provider.

### Indirect Use Cases of AMS
> [!NOTE]
> Indirect use of AMS via other MIS components
1. UC1 **Manual capability registration**
   - Registration of manufacturer capability information via factory connectors.
   - AI-based extraction of manufacturer capability information, based on machine specifications.
2. UC2 **Automatic capability registration / crawling**
   - The manufacturers' production capabilities are automatically read out by querying the factory connectors.
4. UC4 **Search for potential suppliers/supply chains**
- Identification of potential suppliers/supply chains for a given production process.

### Link to the other MIS components

| Components    | Goals         | URL           |
| ------------- | ------------- | ------------- |
| **Supplier Knowledge Base (SKB)** | Knowledge base for manufacturer/supplier information e.g. capabilities, properties, etc. | [MIS-SKB](https://github.com/FraunhoferIOSB/MIS-SKB)  |
| **Asset Management and Refinement Application (AMARA)**  | Automatically derives manufacturing capabilities from machine specifications with Large Language Models (LLM)  | [MIS-AMARA](https://github.com/FraunhoferIOSB/MIS-AMARA) |
| **Asset Management Service (AMS)**  | Interface to manage asset information like machines, manufacturing capabilities, etc. within the knowledge base |[MIS-AMS](https://github.com/FraunhoferIOSB/MIS-AMS)  |
| **Search Engine (SE)**  | Provision of manufacturer information such as production capabilities for a given process description  | [MIS-SE](https://github.com/FraunhoferIOSB/MIS-SE)  |

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions are **greatly appreciated**.
You can find our contribution guidelines [here](CONTRIBUTING.md)

## Contact

info-disc-ecosystem@iosb.fraunhofer.de

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.
