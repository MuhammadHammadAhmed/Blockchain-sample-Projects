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
 * @param {org.example.basic.addFacility} tx The sample transaction instance.
 * @transaction
 */
async function addFacility(tx) {  // eslint-disable-line no-unused-vars

  const NS = 'org.example.basic';
  const factory =  getFactory();
  let facilityId= tx.facilityId;
  const facility = factory.newResource(NS,'Facility',facilityId);
  facility.facilityName=tx.facilityName;
  facility.facilityType=tx.facilityType;
  facility.rate = tx.rate;
  facility.limit = tx.limit;
  facility.outstanding= tx.outstanding;
  facility.expiry= tx.expiry;
  const facilityRegistry = await getAssetRegistry(NS + '.Facility');

  await facilityRegistry.addAll([facility]);
}

/**
* Sample transaction processor function.
* @param {org.example.basic.updateBBFS} updt The sample transaction instance.
* @transaction
*/
async function updateBBFS(updt){

  const NS = 'org.example.basic'; // namespace
  const factory =  getFactory();
  let bbfs = updt.bbfs;
var limits=0;
let results = await query('getFunded');
   for (let n = 0; n < results.length; n++) {
      let facility = results[n];
      limits = limits+ facility.limit;
    }
                         
bbfs.fundedLimits =limits;
  
  const bbfsRegistry = await getAssetRegistry(NS + '.BBFS');

  await bbfsRegistry.updateAll([bbfs]);
}