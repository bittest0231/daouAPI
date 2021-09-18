package com.organization.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.organization.demo.entity.MemberEntity;
import com.organization.demo.entity.OnlyOrganizationResult;
import com.organization.demo.entity.OnlyOrganizationWithParentResult;
import com.organization.demo.entity.OrganizationResult;
import com.organization.demo.entity.OrganizationWithParentResult;
import com.organization.demo.entity.OrganizationsEntity;
import com.organization.demo.exception.InvalidDataException;
import com.organization.demo.model.DeptModel;
import com.organization.demo.repository.MemberRepository;
import com.organization.demo.repository.OrganizationsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationsService {
	
	@Autowired
	private final OrganizationsRepository OrgRepo;
	
	@Autowired
	private final MemberRepository MemRepo;
	
	// 전체 부서, 멤버 모두 검색
	@Transactional
	public OrganizationResult getOrganizations() throws Exception{
		
//		final List<OrganizationsEntity> list = OrgRepo.findByCode("100");
//		return list.stream().map(OrganizationResult::new).collect(Collectors.toList()).get(0);
		
		long id = OrgRepo.findByType("Company").get().getId();
		
		return new OrganizationResult(
				OrgRepo
				.findById(id)
				.orElseThrow(()-> new InvalidDataException("일치하는 부서가 존재하지 않습니다.")
			)
		);
	}
	
	// 전체 부서만 검색
	@Transactional
	public OnlyOrganizationResult getOnlyOrganizations() throws Exception{
		
//		final List<OrganizationsEntity> list = OrgRepo.findByCode("100");
//		return list.stream().map(OnlyOrganizationResult::new).collect(Collectors.toList()).get(0);
		long id = OrgRepo.findByType("Company").get().getId();
		
		return new OnlyOrganizationResult(
				OrgRepo
				.findById(id)
				.orElseThrow(()-> new InvalidDataException("일치하는 부서가 존재하지 않습니다.")
			)
		);
	}
		
	// 특정 부서 기준으로 부서, 부서원 검색 
	@Transactional
	public OrganizationResult getOrganizations(String deptCode) throws Exception{
		
//		final List<OrganizationsEntity> list = OrgRepo.findByCode(deptCode);
//		return list.stream().map(OrganizationResult::new).collect(Collectors.toList()).get(0);
		
		if("".equals(deptCode))
			throw new InvalidDataException("요청값이 적절하지 않습니다.");
		
		return new OrganizationResult(
				OrgRepo
				.findByCode(deptCode)
				.orElseThrow(()-> new InvalidDataException("일치하는 부서가 존재하지 않습니다.")
			)
		);
	}
	
	// 특정 부서 기준으로 부서만 검색
	@Transactional
	public OnlyOrganizationResult getOnlyOrganizations(String deptCode) throws Exception{
		
//		final List<OrganizationsEntity> list = OrgRepo.findByCode(deptCode);
//		return list.stream().map(OnlyOrganizationResult::new).collect(Collectors.toList()).get(0);
		
		if("".equals(deptCode))
			throw new InvalidDataException("요청값이 적절하지 않습니다.");
		
		return new OnlyOrganizationResult(
				OrgRepo
				.findByCode(deptCode)
				.orElseThrow(()-> new InvalidDataException("일치하는 부서가 존재하지 않습니다.")
			)
		);
	}
	
	/** 키워드 관련 검색**/
	
	// 특정 키워드로 검색 
	public List<Object> getOrgFromKeyword(String searchType, String keyword) throws Exception{
		
		if("dept".equals(searchType.toLowerCase())) {
			// 부서 키워드로 검색
			final List<OrganizationsEntity> list = OrgRepo.findByNameContains(keyword);
			
			return list.stream().map(OnlyOrganizationWithParentResult::new).collect(Collectors.toList());
			
		}else if("member".equals(searchType.toLowerCase())) {
			// 부서원 키워드로 검색
			final List<MemberEntity> list = MemRepo.findByNameContains(keyword);
			
			return list.stream().map(OrganizationWithParentResult::new).collect(Collectors.toList());
			
		}else {
			throw new InvalidDataException("searchType 값이 적절하지 않습니다.");
		}
		
	}
	
	// 하나의 부서 가져오기
	public OrganizationsEntity getDeptOne(Long id) throws Exception {
		
		return OrgRepo.findById(id)
				.orElseThrow(()-> 
				new InvalidDataException("일치하는 부서가 존재하지 않습니다.")
			);
	}
	
	
	public List<OrganizationsEntity> getDeptMany(List<Long> idList) throws Exception {
		
		List<OrganizationsEntity> result = OrgRepo.findByIdIn(idList);
		
		return result;
	}
	
	// 부서 추가
	@Transactional
	public OrganizationsEntity createDept(DeptModel model) throws Exception {
		
		// 입력에 필요로 하는 값이 null 이거나 빈값인 경우 체크
		if(model.getCode() == null || "".equals(model.getCode())  
			|| model.getName() == null || "".equals(model.getName())
			|| model.getType() == null || "".equals(model.getType()) 
			|| model.getParentId() == null) 
		{
			throw new InvalidDataException("요청값이 적절하지 않습니다.");
		}
		
		OrganizationsEntity parentEntity = null;
		try {
			parentEntity = getDeptOne(model.getParentId());
			
		}catch(InvalidDataException ide) {
			throw new InvalidDataException("일치하는 부모코드가 없습니다.");
			
		}catch(Exception e) {
			throw new Exception();
		}
		OrganizationsEntity result = OrgRepo.save(
				OrganizationsEntity
				.builder()
				.code(model.getCode())
				.name(model.getName())
				.type(model.getType())
				.parent(parentEntity)
				.build()
			); 
		return result; 
	}
	
	// 부서 업데이트
	public OrganizationsEntity updateDept(Long id, DeptModel model) throws Exception  {
		
		// 입력에 필요로 하는 값이 공백값인 경우 체크
		if("".equals(model.getCode()) || "".equals(model.getName()) || "".equals(model.getType()) ) 
		{
			throw new InvalidDataException("요청값이 적절하지 않습니다.");
		}
		
		
		OrganizationsEntity entity = null;
		
		try {
			entity = getDeptOne(id);
			
		} catch(InvalidDataException ide){
			throw new InvalidDataException(ide.getMessage());
			
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		
		// 각각의 정보가 다른경우만 set 해준다
		if( !entity.getName().equals(model.getName()) ) {
			entity.setName(model.getName());
		}
		if( !entity.getCode().equals(model.getCode()) ) {
			entity.setCode(model.getCode());
		}
		if( !entity.getType().equals(model.getType()) ) {
			entity.setType(model.getType());
		}
		if( entity.getParent().getId() != model.getParentId()) {
			
			OrganizationsEntity parentEntity = null;
			try {
				parentEntity = getDeptOne(model.getParentId());
				
			} catch(InvalidDataException ide) {
				throw new InvalidDataException("일치하는 부모코드가 없습니다.");
				
			}catch(Exception e) {
				throw new Exception();
			}
			
			entity.setParent(parentEntity);
		}
		
		return OrgRepo.save(entity);
	}
	
	// 부서 삭제
	@Transactional
	public void deleteDept(Long id) throws Exception  {
		
		OrganizationsEntity entity = null;
		
		try {
			entity = getDeptOne(id);
			
		} catch(InvalidDataException ide) {
			throw new InvalidDataException(ide.getMessage());
			
		}catch(Exception e) {
			throw new Exception();
		}
		
		if(entity.getMembers().size() > 0) {
			throw new InvalidDataException("부서에 속한 부서원이 존재합니다.");
		}
		if(entity.getChildren().size() > 0) {
			throw new InvalidDataException("하위 부서가 존재합니다.");
		}
		
		OrgRepo.deleteById(id);
	}
	
	
	
	
}
