/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* global getAssetRegistry getFactory emit */


/**
 * Sample transaction processor function.
 * @param {org.example.basic.initFarmInput} arg The sample transaction instance.
 * @transaction
 */
async function initFarmInput(arg){
  
  const NS= 'org.example.basic';// namesspace
  const factory = getFactory();
  let productId = arg.productId;
  let  farmInput =factory.newResource(NS,'FarmInput',productId);
  
  
  farmInput.product = arg.product;
  
  farmInput.quantity = arg.quantity;
  farmInput.unit = arg.unit;
  farmInput.costPrice =arg.costPrice;
  farmInput.status = "instantiated";
  farmInput.unit=arg.unit;
  farmInput.costPrice =arg.costPrice;
  farmInput.issuer =arg.issuer;
  farmInput.authorized = arg.issuer;
  farmInput.currentOwner= arg.issuer;
  
  const assetregistry =await getAssetRegistry(NS+'.FarmInput');
  await assetregistry.add(farmInput);
 
}
/**
 *  transaction to book asset.
 * @param {org.example.basic.bookAsset} arg The sample transaction instance.
 * @transaction
 */
async function bookAsset(arg) {
  
   const NS = 'org.example.basic';// namesspace
 const asset =arg.underlyingAsset;
  let authorize = arg.buyer;
  asset.authorized = authorize;
  asset.status="waad";
  
  const assetRegistry =await getAssetRegistry(NS+'.FarmInput');
   await assetRegistry.update(asset);   
//events-  
  let factory = getFactory();
  let event = factory.newEvent(NS, 'BookFarmInput');
    emit(event);
}
/**
* Transaction to initialize Murabaha
* @param {org.example.basic.initMurabaha} arg  the transaction instance
* @transaction
*/
async function initMurabaha(arg) {
  
  const NS ='org.example.basic';
 

//let asset = await query(q1);
let asset = (await query ('selectFarmInput',{pId:arg.underlyingAsset}))[0];
  console.log(asset);
  let buyer= arg.buyer;
  let seller = asset.authorized;
  let date = new Date();
  let contractId =buyer.getIdentifier() + "|" + asset.authorized.getIdentifier()+"|"+ +asset.productId +"|" + date.toISOString();
  
  
  let factory = getFactory();
  let  murabaha = await factory.newResource(NS,'Murabaha',contractId);
 let paymentDate =new Date();
  paymentDate.setDate(paymentDate.getDate() +180);
   murabaha.contractDate=date;
  murabaha.seller= seller;
  murabaha.buyer =buyer;
  asset.currentOwner = seller;
  murabaha.underlyingAsset =asset.productId;
  murabaha.costPrice = asset.costPrice;
  murabaha.sellingPrice =asset.currentPrice;
  murabaha.paymentDate= paymentDate;
  murabaha.buyerSignature="unsigned";
   murabaha.sellerSignature="signed";
  asset.status="Issued";
  let assetRegistry =await getAssetRegistry(NS+'.Murabaha');
  await assetRegistry.add(murabaha);
  //updating the FarmInput
  
  console.log(asset);
  console.log(asset.costPrice);
  console.log(asset);
  return getAssetRegistry(NS +'.FarmInput')
  .then(function (farmInputRegistry) {
    
    return farmInputRegistry.update(asset);
  })
  .catch(function (error) {
console.log("Error in init Murabaha")  });
   
}

/**
* Transaction to accept Murabaha
* @param {org.example.basic.acceptMurabaha} arg  the transaction instance
* @transaction
*/
async function acceptMurabaha(arg) {
  const NS ='org.example.basic';
  let murabaha = arg.contract;
  let buyer =  arg.farmer;
  let asset = (await query ('selectFarmInput',{pId:murabaha.asset}))[0];
  murabaha.buyerSignature="signed";
  murabaha.costPrice= asset.currentPrice;
  murabaha.contractDate = new Date();
 asset.status= "Sold";
  asset.currentOwner= buyer;
   let assetRegistry =await getAssetRegistry(NS+'.Murabaha');
  await assetRegistry.update(murabaha)
  
  
  return getAssetRegistry(NS +'.FarmInput')
  .then(function (farmInputRegistry) {
    
     farmInputRegistry.update(asset);
  });
  
  

  
}
/**
* Transaction to update  Price
* @param {org.example.basic.updatePrice} arg  the transaction instance
* @transaction
*/
async function updatePrice(arg) {
  
  const NS ='org.example.basic';
 

let asset = (await query ('selectFarmInput',{pId:arg.productId}))[0];
  
  console.log(asset);
  asset.currentPrice = arg.price;
  
      
 return getAssetRegistry(NS +'.FarmInput')//update registry
  .then(function (farmInputRegistry) {
    
     farmInputRegistry.update(asset);
  });  

}