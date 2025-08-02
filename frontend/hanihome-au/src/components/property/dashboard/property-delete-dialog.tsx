'use client';

import React, { Fragment, useState } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { ExclamationTriangleIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { PropertyWithStats } from '../property-dashboard';

interface PropertyDeleteDialogProps {
  property: PropertyWithStats;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

export default function PropertyDeleteDialog({
  property,
  isOpen,
  onClose,
  onConfirm,
}: PropertyDeleteDialogProps) {
  const [isDeleting, setIsDeleting] = useState(false);
  const [confirmText, setConfirmText] = useState('');

  const expectedConfirmText = property.title;
  const isConfirmValid = confirmText === expectedConfirmText;

  const handleConfirm = async () => {
    if (!isConfirmValid) return;

    setIsDeleting(true);
    try {
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      onConfirm();
    } catch (error) {
      console.error('Failed to delete property:', error);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleClose = () => {
    if (!isDeleting) {
      setConfirmText('');
      onClose();
    }
  };

  return (
    <Transition.Root show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={handleClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
        </Transition.Child>

        <div className="fixed inset-0 z-10 overflow-y-auto">
          <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
              enterTo="opacity-100 translate-y-0 sm:scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 translate-y-0 sm:scale-100"
              leaveTo="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            >
              <Dialog.Panel className="relative transform overflow-hidden rounded-lg bg-white px-4 pb-4 pt-5 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:p-6">
                <div className="absolute right-0 top-0 pr-4 pt-4">
                  <button
                    type="button"
                    className="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
                    onClick={handleClose}
                    disabled={isDeleting}
                  >
                    <span className="sr-only">닫기</span>
                    <XMarkIcon className="h-6 w-6" aria-hidden="true" />
                  </button>
                </div>

                <div className="sm:flex sm:items-start">
                  <div className="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10">
                    <ExclamationTriangleIcon className="h-6 w-6 text-red-600" aria-hidden="true" />
                  </div>
                  <div className="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
                    <Dialog.Title as="h3" className="text-base font-semibold leading-6 text-gray-900">
                      매물 삭제 확인
                    </Dialog.Title>
                    <div className="mt-2">
                      <p className="text-sm text-gray-500">
                        이 작업은 되돌릴 수 없습니다. 정말로 다음 매물을 삭제하시겠습니까?
                      </p>
                      
                      {/* Property Info Card */}
                      <div className="mt-4 rounded-lg border border-gray-200 bg-gray-50 p-4">
                        <div className="flex items-start space-x-4">
                          {property.images && property.images.length > 0 ? (
                            <img
                              src={property.images[0]}
                              alt={property.title}
                              className="h-16 w-16 rounded-lg object-cover"
                            />
                          ) : (
                            <div className="h-16 w-16 rounded-lg bg-gray-200 flex items-center justify-center">
                              <span className="text-gray-400 text-xs">이미지 없음</span>
                            </div>
                          )}
                          <div className="flex-1 min-w-0">
                            <h4 className="text-sm font-medium text-gray-900">{property.title}</h4>
                            <p className="text-sm text-gray-500 truncate">{property.address}</p>
                            <div className="mt-1 flex items-center space-x-4 text-xs text-gray-500">
                              <span>조회수: {property.views}</span>
                              <span>문의: {property.inquiries}</span>
                              <span>관심: {property.favorites}</span>
                            </div>
                          </div>
                        </div>
                      </div>

                      {/* Warning Messages */}
                      <div className="mt-4 space-y-2">
                        <div className="rounded-md bg-yellow-50 p-3">
                          <div className="flex">
                            <ExclamationTriangleIcon className="h-5 w-5 text-yellow-400" aria-hidden="true" />
                            <div className="ml-3">
                              <h3 className="text-sm font-medium text-yellow-800">
                                주의사항
                              </h3>
                              <div className="mt-2 text-sm text-yellow-700">
                                <ul className="list-disc space-y-1 pl-5">
                                  <li>매물과 관련된 모든 데이터가 영구적으로 삭제됩니다</li>
                                  <li>문의 내역과 즐겨찾기 정보도 함께 삭제됩니다</li>
                                  <li>업로드된 이미지들도 모두 삭제됩니다</li>
                                  <li>이 작업은 취소할 수 없습니다</li>
                                </ul>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>

                      {/* Confirmation Input */}
                      <div className="mt-4">
                        <label htmlFor="confirmText" className="block text-sm font-medium text-gray-700">
                          삭제를 확인하려면 매물명을 정확히 입력해주세요:
                        </label>
                        <div className="mt-1">
                          <code className="text-sm bg-gray-100 px-2 py-1 rounded border">
                            {expectedConfirmText}
                          </code>
                        </div>
                        <input
                          type="text"
                          id="confirmText"
                          value={confirmText}
                          onChange={(e) => setConfirmText(e.target.value)}
                          className={`mt-2 block w-full rounded-md shadow-sm sm:text-sm ${
                            confirmText && !isConfirmValid
                              ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                              : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'
                          }`}
                          placeholder="매물명을 입력하세요"
                          disabled={isDeleting}
                        />
                        {confirmText && !isConfirmValid && (
                          <p className="mt-1 text-sm text-red-600">
                            매물명이 일치하지 않습니다.
                          </p>
                        )}
                        {isConfirmValid && (
                          <p className="mt-1 text-sm text-green-600">
                            ✓ 매물명이 확인되었습니다.
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
                
                <div className="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
                  <button
                    type="button"
                    className="inline-flex w-full justify-center rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-red-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600 sm:ml-3 sm:w-auto disabled:opacity-50 disabled:cursor-not-allowed"
                    onClick={handleConfirm}
                    disabled={!isConfirmValid || isDeleting}
                  >
                    {isDeleting ? (
                      <div className="flex items-center">
                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        삭제 중...
                      </div>
                    ) : (
                      '삭제'
                    )}
                  </button>
                  <button
                    type="button"
                    className="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto"
                    onClick={handleClose}
                    disabled={isDeleting}
                  >
                    취소
                  </button>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
}