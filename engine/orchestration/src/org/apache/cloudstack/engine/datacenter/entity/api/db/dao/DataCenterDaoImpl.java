// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
// Automatically generated by addcopyright.py at 04/03/2012
package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;
import javax.persistence.TableGenerator;

import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import org.apache.cloudstack.engine.datacenter.entity.api.db.DataCenterVO;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cloud.org.Grouping;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SequenceFetcher;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.net.NetUtils;

/**
 * @config
 * {@table
 *    || Param Name | Description | Values | Default ||
 *    || mac.address.prefix | prefix to attach to all public and private mac addresses | number | 06 ||
 *  }
 **/
@Component(value="EngineDataCenterDao")
@Local(value={DataCenterDao.class})
public class DataCenterDaoImpl extends GenericDaoBase<DataCenterVO, Long> implements DataCenterDao {
    private static final Logger s_logger = Logger.getLogger(DataCenterDaoImpl.class);

    protected SearchBuilder<DataCenterVO> NameSearch;
    protected SearchBuilder<DataCenterVO> ListZonesByDomainIdSearch;
    protected SearchBuilder<DataCenterVO> PublicZonesSearch;
    protected SearchBuilder<DataCenterVO> ChildZonesSearch;
    protected SearchBuilder<DataCenterVO> DisabledZonesSearch;
    protected SearchBuilder<DataCenterVO> TokenSearch;
    protected SearchBuilder<DataCenterVO> StateChangeSearch;
    protected SearchBuilder<DataCenterVO> UUIDSearch;

    protected long _prefix;
    protected Random _rand = new Random(System.currentTimeMillis());
    protected TableGenerator _tgMacAddress;

    @Inject protected DcDetailsDao _detailsDao;


    @Override
    public DataCenterVO findByName(String name) {
        SearchCriteria<DataCenterVO> sc = NameSearch.create();
        sc.setParameters("name", name);
        return findOneBy(sc);
    }

    
    @Override
    public DataCenterVO findByToken(String zoneToken){
        SearchCriteria<DataCenterVO> sc = TokenSearch.create();
        sc.setParameters("zoneToken", zoneToken);
        return findOneBy(sc);
    }

    @Override
    public List<DataCenterVO> findZonesByDomainId(Long domainId){
        SearchCriteria<DataCenterVO> sc = ListZonesByDomainIdSearch.create();
        sc.setParameters("domainId", domainId);
        return listBy(sc);    	
    }

    @Override
    public List<DataCenterVO> findZonesByDomainId(Long domainId, String keyword){
        SearchCriteria<DataCenterVO> sc = ListZonesByDomainIdSearch.create();
        sc.setParameters("domainId", domainId);
        if (keyword != null) {
            SearchCriteria<DataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        return listBy(sc);    	
    }

    @Override
    public List<DataCenterVO> findChildZones(Object[] ids, String keyword){
        SearchCriteria<DataCenterVO> sc = ChildZonesSearch.create();
        sc.setParameters("domainid", ids);
        if (keyword != null) {
            SearchCriteria<DataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        return listBy(sc);  
    }

    @Override
    public List<DataCenterVO> listPublicZones(String keyword){
        SearchCriteria<DataCenterVO> sc = PublicZonesSearch.create();
        if (keyword != null) {
            SearchCriteria<DataCenterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }
        //sc.setParameters("domainId", domainId);
        return listBy(sc);    	    	
    }

    @Override
    public List<DataCenterVO> findByKeyword(String keyword){
        SearchCriteria<DataCenterVO> ssc = createSearchCriteria();
        ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
        ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
        return listBy(ssc);
    }


    @Override
    public String[] getNextAvailableMacAddressPair(long id) {
        return getNextAvailableMacAddressPair(id, 0);
    }

    @Override
    public String[] getNextAvailableMacAddressPair(long id, long mask) {
        SequenceFetcher fetch = SequenceFetcher.getInstance();

        long seq = fetch.getNextSequence(Long.class, _tgMacAddress, id);
        seq = seq | _prefix | ((id & 0x7f) << 32);
        seq |= mask;
        seq |= ((_rand.nextInt(Short.MAX_VALUE) << 16) & 0x00000000ffff0000l);
        String[] pair = new String[2];
        pair[0] = NetUtils.long2Mac(seq);
        pair[1] = NetUtils.long2Mac(seq | 0x1l << 39);
        return pair;
    }


    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        if (!super.configure(name, params)) {
            return false;
        }

        String value = (String)params.get("mac.address.prefix");
        _prefix = (long)NumbersUtil.parseInt(value, 06) << 40;

        return true;
    }

    protected DataCenterDaoImpl() {
        super();
        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        NameSearch.done();

        ListZonesByDomainIdSearch = createSearchBuilder();
        ListZonesByDomainIdSearch.and("domainId", ListZonesByDomainIdSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        ListZonesByDomainIdSearch.done();

        PublicZonesSearch = createSearchBuilder();
        PublicZonesSearch.and("domainId", PublicZonesSearch.entity().getDomainId(), SearchCriteria.Op.NULL);
        PublicZonesSearch.done();        

        ChildZonesSearch = createSearchBuilder();
        ChildZonesSearch.and("domainid", ChildZonesSearch.entity().getDomainId(), SearchCriteria.Op.IN);
        ChildZonesSearch.done();

        DisabledZonesSearch = createSearchBuilder();
        DisabledZonesSearch.and("allocationState", DisabledZonesSearch.entity().getAllocationState(), SearchCriteria.Op.EQ);
        DisabledZonesSearch.done();

        TokenSearch = createSearchBuilder();
        TokenSearch.and("zoneToken", TokenSearch.entity().getZoneToken(), SearchCriteria.Op.EQ);
        TokenSearch.done();

        StateChangeSearch = createSearchBuilder();
        StateChangeSearch.and("id", StateChangeSearch.entity().getId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("state", StateChangeSearch.entity().getState(), SearchCriteria.Op.EQ);
        StateChangeSearch.done();

        UUIDSearch = createSearchBuilder();
        UUIDSearch.and("uuid", UUIDSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        UUIDSearch.done();


        _tgMacAddress = _tgs.get("macAddress");
        assert _tgMacAddress != null : "Couldn't get mac address table generator";
    }

    @Override @DB
    public boolean update(Long zoneId, DataCenterVO zone) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        boolean persisted = super.update(zoneId, zone);
        if (!persisted) {
            return persisted;
        }
        saveDetails(zone);
        txn.commit();
        return persisted;
    }

    @Override
    public void loadDetails(DataCenterVO zone) {
        Map<String, String> details =_detailsDao.findDetails(zone.getId());
        zone.setDetails(details);
    }

    @Override
    public void saveDetails(DataCenterVO zone) {
        Map<String, String> details = zone.getDetails();
        if (details == null) {
            return;
        }
        _detailsDao.persist(zone.getId(), details);
    }

    @Override
    public List<DataCenterVO> listDisabledZones(){
        SearchCriteria<DataCenterVO> sc = DisabledZonesSearch.create();
        sc.setParameters("allocationState", Grouping.AllocationState.Disabled);

        List<DataCenterVO> dcs =  listBy(sc);

        return dcs;
    }

    @Override
    public List<DataCenterVO> listEnabledZones(){
        SearchCriteria<DataCenterVO> sc = DisabledZonesSearch.create();
        sc.setParameters("allocationState", Grouping.AllocationState.Enabled);

        List<DataCenterVO> dcs =  listBy(sc);

        return dcs;
    }

    @Override
    public DataCenterVO findByTokenOrIdOrName(String tokenOrIdOrName) {
        DataCenterVO result = findByToken(tokenOrIdOrName);
        if (result == null) {
            result = findByName(tokenOrIdOrName);
            if (result == null) {
                try {
                    Long dcId = Long.parseLong(tokenOrIdOrName);
                    return findById(dcId);
                } catch (NumberFormatException nfe) {

                }
            }
        }
        return result;
    }

    @Override
    public boolean remove(Long id) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        DataCenterVO zone = createForUpdate();
        zone.setName(null);

        update(id, zone);

        boolean result = super.remove(id);
        txn.commit();
        return result;
    }


    @Override
    public boolean updateState(State currentState, Event event, State nextState, DataCenterResourceEntity zoneEntity, Object data) {

        DataCenterVO vo = findById(zoneEntity.getId());

        Date oldUpdatedTime = vo.getLastUpdated();

        SearchCriteria<DataCenterVO> sc = StateChangeSearch.create();
        sc.setParameters("id", vo.getId());
        sc.setParameters("state", currentState);

        UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "lastUpdated", new Date());

        int rows = update(vo, sc);

        if (rows == 0 && s_logger.isDebugEnabled()) {
            DataCenterVO dbDC = findByIdIncludingRemoved(vo.getId());
            if (dbDC != null) {
                StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=").append(dbDC.getId()).append("; state=").append(dbDC.getState()).append(";updatedTime=")
                .append(dbDC.getLastUpdated());
                str.append(": New Data={id=").append(vo.getId()).append("; state=").append(nextState).append("; event=").append(event).append("; updatedTime=").append(vo.getLastUpdated());
                str.append(": stale Data={id=").append(vo.getId()).append("; state=").append(currentState).append("; event=").append(event).append("; updatedTime=").append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update dataCenter: id=" + vo.getId() + ", as there is no such dataCenter exists in the database anymore");
            }
        }
        return rows > 0;

    }


}
