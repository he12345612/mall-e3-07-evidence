package com.macro.mall.portal.sdc;

import com.macro.mall.mapper.UmsMemberReceiveAddressMapper;
import com.macro.mall.model.UmsMember;
import com.macro.mall.model.UmsMemberReceiveAddress;
import com.macro.mall.model.UmsMemberReceiveAddressExample;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.impl.UmsMemberReceiveAddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * E3-07 specification tests for updating a member's default address.
 *
 * <p>The real service is exercised while the member lookup and persistence
 * boundary are replaced with test doubles.</p>
 */
class E307AddressDefaultUpdateTest {
    private static final Long CURRENT_MEMBER_ID = 101L;

    private UmsMemberReceiveAddressServiceImpl service;
    private UmsMemberReceiveAddressMapper addressMapper;

    @BeforeEach
    void setUp() {
        service = new UmsMemberReceiveAddressServiceImpl();
        UmsMemberService memberService = mock(UmsMemberService.class);
        addressMapper = mock(UmsMemberReceiveAddressMapper.class);

        UmsMember member = new UmsMember();
        member.setId(CURRENT_MEMBER_ID);
        when(memberService.getCurrentMember()).thenReturn(member);
        ReflectionTestUtils.setField(service, "memberService", memberService);
        ReflectionTestUtils.setField(service, "addressMapper", addressMapper);
    }

    @Test
    void missingDefaultTargetDoesNotWrite() {
        when(addressMapper.countByExample(any())).thenReturn(0L);
        when(addressMapper.updateByExampleSelective(any(), any())).thenReturn(1, 0);

        int result = service.update(999L, address(1));

        assertEquals(0, result);
        verify(addressMapper, never()).updateByExampleSelective(any(), any());
    }

    @Test
    void addressOwnedByAnotherMemberDoesNotWrite() {
        when(addressMapper.countByExample(any())).thenReturn(0L);
        when(addressMapper.updateByExampleSelective(any(), any())).thenReturn(1, 0);

        int result = service.update(888L, address(1));

        assertEquals(0, result);
        verify(addressMapper, never()).updateByExampleSelective(any(), any());
    }

    @Test
    void validNonDefaultTargetBecomesDefaultInRequiredWriteOrder() {
        when(addressMapper.countByExample(any())).thenReturn(1L);
        when(addressMapper.updateByExampleSelective(any(), any())).thenReturn(1);
        ArgumentCaptor<UmsMemberReceiveAddress> rowCaptor =
                ArgumentCaptor.forClass(UmsMemberReceiveAddress.class);
        ArgumentCaptor<UmsMemberReceiveAddressExample> exampleCaptor =
                ArgumentCaptor.forClass(UmsMemberReceiveAddressExample.class);

        int result = service.update(321L, address(1));

        assertEquals(1, result);
        verify(addressMapper, times(2)).updateByExampleSelective(
                rowCaptor.capture(), exampleCaptor.capture());

        List<UmsMemberReceiveAddress> rows = rowCaptor.getAllValues();
        List<UmsMemberReceiveAddressExample> examples = exampleCaptor.getAllValues();
        assertEquals(0, rows.get(0).getDefaultStatus());
        assertEquals(1, rows.get(1).getDefaultStatus());
        assertHasCriterion(examples.get(0), "member_id =", CURRENT_MEMBER_ID);
        assertHasCriterion(examples.get(0), "default_status =", 1);
        assertHasCriterion(examples.get(1), "member_id =", CURRENT_MEMBER_ID);
        assertHasCriterion(examples.get(1), "id =", 321L);
    }

    @Test
    void alreadyDefaultTargetCanBeUpdatedWithOriginalSemantics() {
        when(addressMapper.countByExample(any())).thenReturn(1L);
        when(addressMapper.updateByExampleSelective(any(), any())).thenReturn(1);

        int result = service.update(321L, address(1));

        assertEquals(1, result);
        verify(addressMapper, times(2)).updateByExampleSelective(
                any(UmsMemberReceiveAddress.class), any(UmsMemberReceiveAddressExample.class));
    }

    @Test
    void ordinaryFieldUpdateDoesNotResetDefaultAddresses() {
        when(addressMapper.countByExample(any())).thenReturn(1L);
        when(addressMapper.updateByExampleSelective(any(), any())).thenReturn(1);
        UmsMemberReceiveAddress request = address(null);
        request.setPhoneNumber("13800138000");
        ArgumentCaptor<UmsMemberReceiveAddress> rowCaptor =
                ArgumentCaptor.forClass(UmsMemberReceiveAddress.class);
        ArgumentCaptor<UmsMemberReceiveAddressExample> exampleCaptor =
                ArgumentCaptor.forClass(UmsMemberReceiveAddressExample.class);

        int result = service.update(321L, request);

        assertEquals(1, result);
        verify(addressMapper).updateByExampleSelective(rowCaptor.capture(), exampleCaptor.capture());
        assertEquals(0, rowCaptor.getValue().getDefaultStatus());
        assertEquals("13800138000", rowCaptor.getValue().getPhoneNumber());
        assertHasCriterion(exampleCaptor.getValue(), "member_id =", CURRENT_MEMBER_ID);
        assertHasCriterion(exampleCaptor.getValue(), "id =", 321L);
    }

    @Test
    void explicitNonDefaultUpdateDoesNotResetOtherDefaults() {
        when(addressMapper.countByExample(any())).thenReturn(1L);
        when(addressMapper.updateByExampleSelective(any(), any())).thenReturn(1);
        ArgumentCaptor<UmsMemberReceiveAddress> rowCaptor =
                ArgumentCaptor.forClass(UmsMemberReceiveAddress.class);
        ArgumentCaptor<UmsMemberReceiveAddressExample> exampleCaptor =
                ArgumentCaptor.forClass(UmsMemberReceiveAddressExample.class);

        int result = service.update(321L, address(0));

        assertEquals(1, result);
        verify(addressMapper).updateByExampleSelective(rowCaptor.capture(), exampleCaptor.capture());
        assertEquals(0, rowCaptor.getValue().getDefaultStatus());
        assertHasCriterion(exampleCaptor.getValue(), "member_id =", CURRENT_MEMBER_ID);
        assertHasCriterion(exampleCaptor.getValue(), "id =", 321L);
    }

    private UmsMemberReceiveAddress address(Integer defaultStatus) {
        UmsMemberReceiveAddress address = new UmsMemberReceiveAddress();
        address.setId(999L);
        address.setDefaultStatus(defaultStatus);
        address.setName("test-address");
        return address;
    }

    private void assertHasCriterion(
            UmsMemberReceiveAddressExample example,
            String condition,
            Object expectedValue) {
        assertTrue(example.getOredCriteria().stream()
                        .flatMap(criteria -> criteria.getAllCriteria().stream())
                        .anyMatch(criterion ->
                                condition.equals(criterion.getCondition())
                                        && expectedValue.equals(criterion.getValue())),
                () -> "missing criterion " + condition + " " + expectedValue);
    }
}
