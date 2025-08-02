'use client';

import React, { useState } from 'react';
import { PropertyOptionsInterface } from '@/components/property/property-options-interface';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

export default function PropertyOptionsTestPage() {
  const [selectedOptions, setSelectedOptions] = useState<string[]>([]);
  const [variant, setVariant] = useState<'default' | 'card' | 'compact'>('default');
  const [columns, setColumns] = useState<1 | 2 | 3 | 4>(3);
  const [maxSelections, setMaxSelections] = useState<number | undefined>(undefined);

  const handleReset = () => {
    setSelectedOptions([]);
  };

  const handlePresetSelect = (preset: string[]) => {
    setSelectedOptions(preset);
  };

  // Preset options for testing
  const basicPreset = ['에어컨', '세탁기', '냉장고', '인터폰'];
  const luxuryPreset = ['에어컨', '세탁기', '냉장고', '식기세척기', '건조기', 'CCTV', '주차장', '엘리베이터'];
  const locationPreset = ['지하철역 근처', '버스정류장 근처', '마트 근처', '편의점 근처'];

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-6xl">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            매물 옵션 체크박스 인터페이스 테스트
          </h1>
          <p className="text-gray-600">
            다양한 스타일과 설정으로 매물 옵션 선택 인터페이스를 테스트해보세요.
          </p>
        </div>

        {/* Controls */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>인터페이스 설정</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              {/* Variant Control */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  스타일 변형
                </label>
                <div className="space-y-2">
                  {(['default', 'card', 'compact'] as const).map((v) => (
                    <label key={v} className="flex items-center">
                      <input
                        type="radio"
                        value={v}
                        checked={variant === v}
                        onChange={(e) => setVariant(e.target.value as typeof variant)}
                        className="mr-2"
                      />
                      <span className="capitalize">{v}</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Columns Control */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  컬럼 수
                </label>
                <div className="space-y-2">
                  {([1, 2, 3, 4] as const).map((c) => (
                    <label key={c} className="flex items-center">
                      <input
                        type="radio"
                        value={c}
                        checked={columns === c}
                        onChange={(e) => setColumns(Number(e.target.value) as typeof columns)}
                        className="mr-2"
                      />
                      <span>{c}컬럼</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Max Selections Control */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  최대 선택 수
                </label>
                <div className="space-y-2">
                  <label className="flex items-center">
                    <input
                      type="radio"
                      checked={maxSelections === undefined}
                      onChange={() => setMaxSelections(undefined)}
                      className="mr-2"
                    />
                    <span>제한 없음</span>
                  </label>
                  {[5, 10, 15].map((max) => (
                    <label key={max} className="flex items-center">
                      <input
                        type="radio"
                        checked={maxSelections === max}
                        onChange={() => setMaxSelections(max)}
                        className="mr-2"
                      />
                      <span>{max}개</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Preset Controls */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  프리셋
                </label>
                <div className="space-y-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handlePresetSelect(basicPreset)}
                    className="w-full text-left justify-start"
                  >
                    기본 옵션
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handlePresetSelect(luxuryPreset)}
                    className="w-full text-left justify-start"
                  >
                    고급 옵션
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handlePresetSelect(locationPreset)}
                    className="w-full text-left justify-start"
                  >
                    위치 옵션
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={handleReset}
                    className="w-full text-left justify-start"
                  >
                    초기화
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Selected Options Summary */}
        {selectedOptions.length > 0 && (
          <Card className="mb-6">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                선택된 옵션
                <Badge variant="secondary">{selectedOptions.length}개</Badge>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {selectedOptions.map((option) => (
                  <Badge key={option} variant="default">
                    {option}
                  </Badge>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        {/* Main Interface */}
        <Card>
          <CardContent className="p-6">
            <PropertyOptionsInterface
              selectedOptions={selectedOptions}
              onOptionsChange={setSelectedOptions}
              maxSelections={maxSelections}
              variant={variant}
              columns={columns}
            />
          </CardContent>
        </Card>

        {/* JSON Output for Development */}
        {process.env.NODE_ENV === 'development' && selectedOptions.length > 0 && (
          <Card className="mt-6 border-dashed">
            <CardHeader>
              <CardTitle className="text-sm text-gray-600">
                개발용: JSON 출력
              </CardTitle>
            </CardHeader>
            <CardContent>
              <pre className="text-xs bg-gray-100 p-4 rounded overflow-auto">
                {JSON.stringify({ selectedOptions }, null, 2)}
              </pre>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}