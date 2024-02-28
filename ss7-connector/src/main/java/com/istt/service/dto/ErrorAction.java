package com.istt.service.dto;

/**
 * @author Amit Bhayani
 * @author sergey vetyutnev
 */
public enum ErrorAction {
  subscriberBusy,
  memoryCapacityExceededFlag, // MNRF
  mobileNotReachableFlag, // MNRF
  notReachableForGprs, // MNRG
  permanentFailure,
  temporaryFailure,

  //    temporaryFailure, permanentFailure;

}
