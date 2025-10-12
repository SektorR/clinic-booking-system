import { Card } from '../../components/common'

const MessagesPage = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Messages</h1>

      <Card>
        <div className="text-center py-12">
          <svg
            className="mx-auto h-12 w-12 text-gray-400 mb-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
            />
          </svg>
          <p className="text-gray-600">Messaging system coming soon</p>
        </div>
      </Card>
    </div>
  )
}

export default MessagesPage
