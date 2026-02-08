import React, { useState, useEffect } from 'react';

function FileExtensionBlocker() {
  const [fixedExtensions, setFixedExtensions] = useState({});
  const [customExtensionInput, setCustomExtensionInput] = useState('');
  const [customExtensions, setCustomExtensions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // API 호출용 함수
  const makeApiCall = async (url, method, body = null) => {
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
    };
    if (body) {
      options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);
    let resJson = null;
    // Attempt to parse JSON regardless of response.ok
    try {
      resJson = await response.json();
    } catch (e) {
      console.error("makeApiCall" + e);
    }

    if (!response.ok) {
      // 서버에서 ApiResponse.error 형태로 응답을 주었을 경우
      if (resJson && resJson.error) {
        // 오류 코드와 메시지를 포함하여 새로운 Error 객체 생성
        const errorCode = resJson.error.code || 'UNKNOWN_ERROR';
        const errorMessage = resJson.error.message || '알 수 없는 오류가 발생했습니다.';
        throw new Error(`[${errorCode}] ${errorMessage}`);
      }
      // JSON 형식이 아니거나 예상치 못한 오류 응답일 경우
      const statusText = response.statusText || '서버 응답 오류';
      throw new Error(`HTTP 오류! 상태: ${response.status} (${statusText})`);
    }

    // 성공적인 응답의 경우, 응답에 데이터 객체가 있다면 반환
    return resJson;
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const fixedExtResponse = await makeApiCall('/api/fixed-extensions', 'GET');
        const transformedFixedData = fixedExtResponse.data.reduce((acc, ext) => {
          acc[ext.name] = { checked: ext.checked, description: ext.description, id: ext.id };
          return acc;
        }, {});
        setFixedExtensions(transformedFixedData);

        const customExtResponse = await makeApiCall('/api/custom-extensions', 'GET');
        setCustomExtensions(customExtResponse.data);

      } catch (e) {
        setError(e.message);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleFixedExtensionChange = async (event) => {
    const { name, checked } = event.target;
    const extensionId = fixedExtensions[name].id;

    setFixedExtensions(prev => ({
      ...prev,
      [name]: { ...prev[name], checked: checked },
    }));

    try {
      await makeApiCall(`/api/fixed-extensions/${extensionId}`, 'PUT', { checked: checked });
    } catch (e) {
      console.error("Error updating fixed extension:", e);
      alert(`고정 확장자 업데이트 오류: ${e.message}`);
      setFixedExtensions(prev => ({
        ...prev,
        [name]: { ...prev[name], checked: !checked },
      }));
    }
  };

  const handleCustomExtensionInputChange = (event) => {
    const originalValue = event.target.value;
    const filteredValue = originalValue.replace(/[^a-zA-Z0-9]/g, '');

    if (originalValue !== filteredValue) {
      alert("특수문자나 한글은 입력 불가능합니다.");
    }
    setCustomExtensionInput(filteredValue);
  };

  const handleAddCustomExtension = async () => {
    const trimmedInput = customExtensionInput.trim();
    if (!trimmedInput) {
      alert("1글자 이상 넣어주세요.");
      setCustomExtensionInput('');
      return;
    }

    // 20자 초과 체크 (Client-side validation for length)
    if (trimmedInput.length > 20) {
      alert("확장자 이름은 20자를 초과할 수 없습니다.");
      setCustomExtensionInput('');
      return;
    }

    // 200개 제한 체크 (Client-side validation for count)
    if (customExtensions.length >= 200) {
      alert("커스텀 확장자는 최대 200개까지만 추가할 수 있습니다.");
      setCustomExtensionInput('');
      return;
    }
    
    const newExtensionName = trimmedInput;

    // Check for duplicate locally before sending to backend to avoid unnecessary API call
    if (customExtensions.some(ext => ext.name.toLowerCase() === newExtensionName.toLowerCase())) {
      alert(`확장자 "${newExtensionName}"은(는) 이미 존재합니다.`);
      setCustomExtensionInput('');
      return;
    }

    try {
      const newExt = await makeApiCall('/api/custom-extensions', 'POST', { name: newExtensionName });
      setCustomExtensions(prev => [...prev, newExt.data]);
      setCustomExtensionInput('');
    } catch (e) {
      console.error("handleAddCustomExtension:", e);
      alert(`커스텀 확장자 추가 오류: ${e.message}`);
    }
  };

  const handleRemoveCustomExtension = async (extensionToRemoveId) => {
    if(!window.confirm("삭제하시겠습니까?")){
      return;
    }

    const originalCustomExtensions = customExtensions;
    setCustomExtensions(prev => prev.filter(ext => ext.id !== extensionToRemoveId));

    try {
      await makeApiCall(`/api/custom-extensions/${extensionToRemoveId}`, 'DELETE');
    } catch (e) {
      console.error("handleRemoveCustomExtension:", e);
      alert(`커스텀 확장자 삭제 오류: ${e.message}`);
      setCustomExtensions(originalCustomExtensions);
    }
  };

  if (isLoading) {
    return <div className="max-w-4xl mx-auto p-8 bg-white border border-gray-200 rounded-lg shadow-sm">
             <p className="text-center text-lg">고정 확장자 불러오는 중...</p>
           </div>;
  }

  if (error) {
    return <div className="max-w-4xl mx-auto p-8 bg-white border border-gray-200 rounded-lg shadow-sm">
             <p className="text-center text-lg text-red-600">에러 발생: {error}</p>
           </div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-8 bg-white border border-gray-200 rounded-lg shadow-sm">
      <header className="border-b-2 border-black pb-4 mb-6">
        <h1 class="text-2xl font-bold flex items-center">
          <span class="mr-2">◎</span> 파일 확장자 차단
        </h1>
      </header>

      <main class="space-y-8">
        <section class="flex items-start">
          <h2 class="w-32 font-bold text-gray-700">고정 확장자</h2>
          <div class="flex flex-wrap gap-4 items-center">
            {Object.keys(fixedExtensions).map((ext) => (
              <label key={ext} className="flex items-center space-x-2 cursor-pointer relative pr-4">
                <input
                  type="checkbox"
                  name={ext}
                  checked={fixedExtensions[ext].checked}
                  onChange={handleFixedExtensionChange}
                  className="w-4 h-4 accent-blue-600"
                />
                <span>{ext}</span>
                <span
                  className="absolute -top-1 right-0 text-xs text-gray-400 cursor-help"
                  title={fixedExtensions[ext].description}
                >
                  ?
                </span>
              </label>
            ))}
          </div>
        </section>

        <section class="flex items-start">
          <h2 class="w-32 font-bold text-gray-700 pt-2">커스텀 확장자</h2>
          <div class="flex-1 space-y-4">
            <div class="flex gap-2">
              <input
                type="text"
                placeholder="확장자 입력"
                maxLength="20"
                value={customExtensionInput}
                onChange={handleCustomExtensionInputChange}
                className="border border-gray-300 rounded px-3 py-1.5 w-64 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
              <button
                onClick={handleAddCustomExtension}
                className="bg-gray-600 text-white px-4 py-1.5 rounded hover:bg-gray-700 transition"
              >
                + 추가
              </button>
            </div>

            <div className="border border-gray-300 rounded-md p-4 min-h-[150px]">
              <div className="flex flex-wrap gap-2">
                {customExtensions.map((ext) => (
                    <div key={ext.id}
                         className="inline-flex items-center bg-white border border-gray-300 rounded px-2 py-1 text-sm">
                      <span>{ext.name}</span>
                      <button
                          onClick={() => handleRemoveCustomExtension(ext.id)}
                          className="ml-2 text-gray-400 hover:text-red-500 font-bold"
                      >
                        X
                      </button>
                    </div>
                ))}
              </div>
              <div className="text-sm text-gray-500 mb-3">
                {customExtensions.length}/200
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default FileExtensionBlocker;